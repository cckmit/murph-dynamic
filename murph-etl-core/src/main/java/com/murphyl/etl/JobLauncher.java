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
import com.murphyl.expr.core.ExpressionEvaluator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * 任务 - 执行器
 *
 * @date: 2021/12/1 14:10
 * @author: murph
 */
public final class JobLauncher implements Callable<JobStatus> {

    private static final Logger logger = LoggerFactory.getLogger(JobLauncher.class);

    private final Set<String> success;

    private final ExecutorService executor;

    private final ExpressionEvaluator exprEvaluator;

    private UUID workflowId;
    private UUID jobId;
    private String[] args;

    public JobLauncher(UUID workflowId) {
        this.workflowId = workflowId;
        int corePoolSize = Environments.getInt("JOB_POOL_CORE_SIZE", 15);
        int maxPoolSize = Environments.getInt("JOB_POOL_MAX_SIZE", 35);
        int keepAliveMinutes = Environments.getInt("JOB_POOL_KEEP_ALIVE_MINUTE", 10);
        int queueSize = Environments.getInt("JOB_POOL_QUEUE_SIZE", 100);
        String poolNamePrefix = Environments.get("JOB_POOL_NAME_PREFIX", "etl-job");
        this.exprEvaluator = TaskStepUtils.getDefaultExprEvaluator();
        this.success = new CopyOnWriteArraySet<>();
        this.executor = ThreadPoolFactory.create(corePoolSize, maxPoolSize, keepAliveMinutes, queueSize, poolNamePrefix);
    }

    public JobStatus launch(String... args) throws ExecutionException, InterruptedException, TimeoutException {
        this.jobId = UUID.randomUUID();
        this.args = args;
        return executor.submit(this).get(2, TimeUnit.HOURS);
    }

    @Override
    public JobStatus call() {
        if (ArrayUtils.isEmpty(args)) {
            logger.error("workflow({}) task({}) invalid args", workflowId, jobId);
            return JobStatus.FAILURE;
        }
        String ts = ArrayUtils.get(args, Environments.CLI_TS_ARG_INDEX);
        String file = ArrayUtils.get(args, Environments.JOB_SCHEMA_ARG_INDEX);
        if (null == file) {
            logger.error("workflow({}) task({}) invalid args", workflowId, jobId);
            return JobStatus.FAILURE;
        }
        logger.info("workflow({}) task({}) fire at: {}", workflowId, jobId, ts);
        return execute(JobSchemaManager.parse(file));
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
            logger.info("workflow({}) job({}) all task completed: {}", workflowId, jobId, success.toArray());
            return success.size() == taskSchemaList.size() ? JobStatus.SUCCESS : JobStatus.FAILURE;
        }).exceptionally(e -> {
            logger.error("workflow({}) job({}) execute error", workflowId, jobId, e);
            return JobStatus.FAILURE;
        });
        try {
            return future.get(2, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.info("workflow({}) job({}) execute error", workflowId, jobId, ExceptionUtils.getRootCause(e));
            return JobStatus.FAILURE;
        }
    }

    private CompletableFuture<JobStatus> createTaskFuture(Map<String, Object> jobParams, TaskSchema task) {
        logger.info("workflow({}) job({}) task({}) [{}] prepared", workflowId, jobId, task.getUUID(), task.getName());
        return CompletableFuture.runAsync(() -> {
            // 检查元数据和依赖
            if (StringUtils.isEmpty(task.getName())) {
                throw new IllegalStateException("task(" + task.getUUID() + ") name can not be null");
            }
            checkTaskDependencies(task.getUUID(), task.getParents());
        }, executor).thenApplyAsync((Void) -> {
            // extract data
            TaskStepSchema extractorSchema = task.getExtractor();
            if (null == extractorSchema) {
                return null;
            }
            Extractor extractorInstance = getTaskStepInstance(extractorSchema, Extractor.class);
            logger.info("workflow({}) job({}) task({}) use [{}] extract data", workflowId, jobId, task.getUUID(), extractorInstance);
            Map<String, Object> stepParams = processTaskStepParams(extractorSchema, jobParams);
            return extractorInstance.extract(extractorSchema.getDsl(), stepParams);
        }, executor).thenApplyAsync(dataframe -> {
            // transform data
            if (null == dataframe || ArrayUtils.isEmpty(task.getTransformers())) {
                return dataframe;
            }
            Transformer transformerInstance;
            Map<String, Object> stepParams;
            for (TaskStepSchema transformerSchema : task.getTransformers()) {
                transformerInstance = getTaskStepInstance(transformerSchema, Transformer.class);
                stepParams = processTaskStepParams(transformerSchema, jobParams);
                logger.info("workflow({}) job({}) task({}) use [{}] transform data", workflowId, jobId, task.getUUID(), transformerInstance);
                dataframe = transformerInstance.transform(transformerSchema.getDsl(), dataframe, stepParams);
            }
            return dataframe;
        }, executor).thenApplyAsync(dataframe -> {
            // load data
            TaskStepSchema loaderSchema = task.getLoader();
            Loader loaderInstance = getTaskStepInstance(loaderSchema, Loader.class);
            Map<String, Object> stepParams = processTaskStepParams(loaderSchema, jobParams);
            logger.info("workflow({}) job({}) task({}) use [{}] load data", workflowId, jobId, task.getUUID(), loaderInstance);
            loaderInstance.load(loaderSchema.getDsl(), dataframe, stepParams);
            success.add(StringUtils.trim(task.getName()));
            return JobStatus.SUCCESS;
        }, executor).exceptionally(e -> {
            // 处理异常
            logger.error("workflow({}) job({}) task({}) execute error", workflowId, jobId, task.getUUID(), ExceptionUtils.getRootCause(e));
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
     * @param taskId
     * @param parents
     */
    private void checkTaskDependencies(UUID taskId, String[] parents) {
        if (ArrayUtils.isEmpty(parents)) {
            return;
        }
        int count = 0;
        Set<String> parsed = new HashSet<>();
        for (String parent : parents) {
            if (StringUtils.isNotBlank(parent)) {
                parsed.add(StringUtils.trim(parent));
            }
        }
        while (!success.containsAll(parsed)) {
            if (count > (120000 / 5 / 1000)) {
                throw new IllegalStateException("task execute error: parent " + Arrays.toString(parsed.toArray()) + " timeout");
            }
            try {
                logger.info("workflow({}) job({}) task({}) check parent {} {}th times", workflowId, jobId, taskId, parsed.toArray(), ++count);
                Thread.currentThread().join(1000 * 5);
            } catch (InterruptedException e) {
                throw new IllegalStateException("task execute error, check parent status error", ExceptionUtils.getRootCause(e));
            }
        }
    }

    protected void shutdown() {
        executor.shutdown();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        new JobLauncher(UUID.randomUUID()).launch(args);
    }
}

