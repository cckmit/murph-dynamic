package com.murphyl.etl;

import com.google.common.collect.Maps;
import com.murphyl.dynamic.Feature;
import com.murphyl.etl.core.job.schema.JobSchema;
import com.murphyl.etl.core.job.schema.JobSchemaManager;
import com.murphyl.etl.core.task.TaskSchema;
import com.murphyl.etl.core.task.TaskStepSchema;
import com.murphyl.etl.core.task.extractor.Extractor;
import com.murphyl.etl.core.task.loader.Loader;
import com.murphyl.etl.core.task.transformer.Transformer;
import com.murphyl.etl.support.Environments;
import com.murphyl.etl.support.JobStatus;
import com.murphyl.etl.utils.TaskStepUtils;
import com.murphyl.etl.utils.ThreadPoolFactory;
import com.murphyl.expression.core.ExpressionEvaluator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务 - 执行器
 *
 * @date: 2021/12/1 14:10
 * @author: murph
 */
public final class JobLauncher implements Callable<JobStatus> {

    private static final Logger logger = LoggerFactory.getLogger(JobLauncher.class);

    private final Set<String> failure;
    private final Set<String> success;

    protected static final ExecutorService EXECUTOR;

    private final ExpressionEvaluator exprEvaluator;

    private UUID workflowId;
    private String jobId;
    private String[] args;

    static {
        int corePoolSize = Environments.getInt("JOB_POOL_CORE_SIZE", 15);
        int maxPoolSize = Environments.getInt("JOB_POOL_MAX_SIZE", 35);
        int keepAliveMinutes = Environments.getInt("JOB_POOL_KEEP_ALIVE_MINUTE", 10);
        int queueSize = Environments.getInt("JOB_POOL_QUEUE_SIZE", 100);
        String poolNamePrefix = Environments.get("JOB_POOL_NAME_PREFIX", "etl-job");
        EXECUTOR = ThreadPoolFactory.create(corePoolSize, maxPoolSize, keepAliveMinutes, queueSize, poolNamePrefix);
    }

    protected static JobStatus launch(UUID workflowId, String... args) throws ExecutionException, InterruptedException, TimeoutException {
        return EXECUTOR.submit(new JobLauncher(workflowId, args)).get(2, TimeUnit.HOURS);
    }

    public JobLauncher(UUID workflowId, String... args) {
        this.workflowId = workflowId;
        this.args = args;
        this.exprEvaluator = TaskStepUtils.getDefaultExprEvaluator();
        this.failure = new CopyOnWriteArraySet<>();
        this.success = new CopyOnWriteArraySet<>();
    }

    @Override
    public JobStatus call() {
        if (ArrayUtils.isEmpty(args)) {
            logger.error("workflow({}) invalid args", workflowId);
            return JobStatus.FAILURE;
        }
        String ts = ArrayUtils.get(args, Environments.CLI_TS_ARG_INDEX);
        this.jobId = ArrayUtils.get(args, Environments.JOB_SCHEMA_ARG_INDEX);
        if (null == jobId) {
            logger.error("workflow({}) invalid args, jobId can not be null", workflowId);
            return JobStatus.FAILURE;
        }
        logger.info("workflow({}) task({}) fire at: {}", workflowId, jobId, ts);
        JobStatus result = execute(JobSchemaManager.parse(jobId));
        logger.info("workflow({}) task({}) completed {}", workflowId, jobId, result);
        return result;
    }

    private JobStatus execute(JobSchema schema) {
        // 检查配置信息
        if (null == schema) {
            logger.error("workflow({}) job({}) params can not be null", workflowId, jobId);
            return JobStatus.FAILURE;
        }
        // 检查子任务队列
        List<TaskSchema> taskSchemaList = schema.getTasks();
        if (null == taskSchemaList || taskSchemaList.isEmpty()) {
            logger.error("workflow({}) job({}) task queue can not be empty", workflowId, jobId);
            return JobStatus.FAILURE;
        }
        Map<String, Object> jobParams = new TreeMap<>();
        // 处理任务参数
        if (null != schema.getParams() && !schema.getParams().isEmpty()) {
            for (Map.Entry<String, String> entry : schema.getParams().entrySet()) {
                try {
                    jobParams.put(entry.getKey(), exprEvaluator.eval(StringUtils.trimToNull(entry.getValue())));
                } catch (Exception e) {
                    logger.error("workflow({}) job({}) prepare params({}) error:", workflowId, jobId, entry, e);
                    return JobStatus.FAILURE;
                }
            }
        }
        // 子任务转换
        CompletableFuture[] tasks = taskSchemaList.stream()
                .map((task) -> createTaskFuture(jobParams, task))
                .toArray(CompletableFuture[]::new);
        // 任务编排
        CompletableFuture<JobStatus> future = CompletableFuture.allOf(tasks).thenApply(Void -> {
            logger.info("workflow({}) job({}) success tasks: {}", workflowId, jobId, failure.toArray());
            for (int i = 0; i < tasks.length; i++) {
                try {
                    if (tasks[i].get() != JobStatus.SUCCESS) {
                        return JobStatus.FAILURE;
                    }
                } catch (Exception e) {
                    logger.error("workflow({}) task({}) get execution result error", workflowId, jobId, e);
                }
            }
            return failure.size() != tasks.length ? JobStatus.FAILURE : JobStatus.SUCCESS;
        });
        try {
            return future.get(2, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.info("workflow({}) job({}) execute error", workflowId, schema.getName(), ExceptionUtils.getRootCause(e));
            return JobStatus.FAILURE;
        }
    }

    private CompletableFuture<JobStatus> createTaskFuture(Map<String, Object> jobParams, TaskSchema task) {
        logger.info("workflow({}) job({}) task({}) [{}] prepared", workflowId, jobId, task.getName(), task.getName());
        return CompletableFuture.runAsync(() -> {
            // 检查元数据和依赖
            if (StringUtils.isEmpty(task.getName())) {
                throw new IllegalStateException("task name can not be null");
            }
            checkTaskDependencies(task.getName(), task.getParents());
        }, EXECUTOR).thenApplyAsync((Void) -> {
            // extract data
            TaskStepSchema extractorSchema = task.getExtractor();
            if (null == extractorSchema) {
                return null;
            }
            Extractor extractorInstance = getTaskStepInstance(extractorSchema, Extractor.class);
            logger.info("workflow({}) job({}) task({}) use [{}] extract data", workflowId, jobId, task.getName(), extractorInstance);
            Map<String, Object> stepParams = processTaskStepParams(extractorSchema, jobParams);
            return extractorInstance.extract(extractorSchema.getDsl(), stepParams);
        }, EXECUTOR).thenApplyAsync(dataframe -> {
            // transform data
            if (null == dataframe || ArrayUtils.isEmpty(task.getTransformers())) {
                return dataframe;
            }
            Transformer transformerInstance;
            Map<String, Object> stepParams;
            for (TaskStepSchema transformerSchema : task.getTransformers()) {
                transformerInstance = getTaskStepInstance(transformerSchema, Transformer.class);
                stepParams = processTaskStepParams(transformerSchema, jobParams);
                logger.info("workflow({}) job({}) task({}) use [{}] transform data", workflowId, jobId, task.getName(), transformerInstance);
                dataframe = transformerInstance.transform(transformerSchema.getDsl(), dataframe, stepParams);
            }
            return dataframe;
        }, EXECUTOR).thenApplyAsync(dataframe -> {
            // load data
            TaskStepSchema loaderSchema = task.getLoader();
            Loader loaderInstance = getTaskStepInstance(loaderSchema, Loader.class);
            Map<String, Object> stepParams = processTaskStepParams(loaderSchema, jobParams);
            logger.info("workflow({}) job({}) task({}) use [{}] load data", workflowId, jobId, task.getName(), loaderInstance);
            loaderInstance.load(loaderSchema.getDsl(), dataframe, stepParams);
            success.add(StringUtils.trim(task.getName()));
            return JobStatus.SUCCESS;
        }, EXECUTOR).exceptionally(e -> {
            failure.add(StringUtils.trim(task.getName()));
            logger.error("workflow({}) job({}) task({}) execute error", workflowId, jobId, task.getName(), ExceptionUtils.getRootCause(e));
            return JobStatus.FAILURE;
        });
    }

    /**
     * 获取任务步骤实例
     *
     * @param schema
     * @param tClass
     * @param <T>
     * @return
     */
    private <T extends Feature> T getTaskStepInstance(TaskStepSchema schema, Class<T> tClass) {
        Objects.requireNonNull(schema, "schema instance can not be null");
        String loaderType = schema.getProperty("type");
        Objects.requireNonNull(schema, "schema type can not be null");
        T loaderInstance = Environments.getFeature(tClass, loaderType);
        Objects.requireNonNull(loaderInstance, "task step instance [" + loaderType + "](" + tClass + ") not found");
        return loaderInstance;
    }

    /**
     * 处理节点参数
     *
     * @param schema
     * @param jobParams
     * @return
     */
    private Map<String, Object> processTaskStepParams(TaskStepSchema schema, Map<String, Object> jobParams) {
        Map<String, Object> stepParams = Maps.newHashMap(jobParams);
        Object value;
        for (Map.Entry<Object, Object> entry : schema.getProperties().entrySet()) {
            value = entry.getValue();
            if (value instanceof String && exprEvaluator.valid(value.toString())) {
                value = exprEvaluator.eval(value.toString(), jobParams, true);
            }
            stepParams.put(Objects.toString(entry.getKey()), value);
        }
        return stepParams;
    }

    /**
     * 检查任务依赖
     *
     * @param taskName
     * @param parents
     */
    private void checkTaskDependencies(String taskName, String[] parents) {
        if (ArrayUtils.isEmpty(parents)) {
            return;
        }
        AtomicInteger counter = new AtomicInteger(0);
        Set<String> parsed = new HashSet<>();
        for (String parent : parents) {
            if (StringUtils.isNotBlank(parent)) {
                parsed.add(StringUtils.trim(parent));
            }
        }
        while (!success.containsAll(parsed)) {
            if (failure.size() > 0) {
                throw new IllegalStateException("task(" + taskName + ") execute error: parent " + Arrays.toString(failure.toArray()) + " error");
            }
            if (counter.intValue() > (120000 / 5 / 1000)) {
                throw new IllegalStateException("task(" + taskName + ") execute error: parent " + Arrays.toString(parsed.toArray()) + " timeout");
            }
            try {
                logger.info("workflow({}) job({}) task({}) check parent {} {}th times", workflowId, jobId, taskName, parsed.toArray(), counter.incrementAndGet());
                Thread.currentThread().join(1000 * 5);
            } catch (InterruptedException e) {
                throw new IllegalStateException("task(" + taskName + ") execute error, check parent status error", ExceptionUtils.getRootCause(e));
            }
        }
    }

    public static void main(String[] args) {
        new JobLauncher(UUID.randomUUID(), args).call();
    }
}

