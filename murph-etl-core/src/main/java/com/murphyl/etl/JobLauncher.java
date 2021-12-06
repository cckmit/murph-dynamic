package com.murphyl.etl;

import com.murphyl.etl.core.job.schema.JobSchema;
import com.murphyl.etl.core.job.schema.JobSchemaManager;
import com.murphyl.etl.core.task.TaskSchema;
import com.murphyl.etl.core.task.TaskStepSchema;
import com.murphyl.etl.core.task.extractor.Extractor;
import com.murphyl.etl.core.task.loader.Loader;
import com.murphyl.etl.core.task.transformer.Transformer;
import com.murphyl.etl.support.Environments;
import com.murphyl.etl.support.JobStatus;
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

    private final ExecutorService executor;

    private static final String EXPRESSION_RESOLVER = "jexl";

    private static ExpressionEvaluator exprEvaluator;

    static {
        exprEvaluator = Environments.getFeature(ExpressionEvaluator.class, EXPRESSION_RESOLVER);
    }

    private UUID workflowId;
    private UUID uuid;
    private String[] args;

    public JobLauncher(UUID workflowId) {
        this.workflowId = workflowId;
        int corePoolSize = Environments.getInt("JOB_POOL_CORE_SIZE", 15);
        int maxPoolSize = Environments.getInt("JOB_POOL_MAX_SIZE", 35);
        int keepAliveMinutes = Environments.getInt("JOB_POOL_KEEP_ALIVE_MINUTE", 10);
        int queueSize = Environments.getInt("JOB_POOL_QUEUE_SIZE", 100);
        String poolNamePrefix = Environments.get("JOB_POOL_NAME_PREFIX", "etl-job");
        executor = ThreadPoolFactory.create(corePoolSize, maxPoolSize, keepAliveMinutes, queueSize, poolNamePrefix);
    }

    public JobStatus launch(String... args) throws ExecutionException, InterruptedException, TimeoutException {
        this.uuid = UUID.randomUUID();
        this.args = args;
        return executor.submit(this).get(2, TimeUnit.HOURS);
    }

    @Override
    public JobStatus call() {
        if (ArrayUtils.isEmpty(args)) {
            logger.error("workflow({}) task({}) invalid args", workflowId, uuid);
            return JobStatus.CLI_ARGS_EMPTY;
        }
        String ts = ArrayUtils.get(args, Environments.CLI_TS_ARG_INDEX);
        String file = ArrayUtils.get(args, Environments.JOB_SCHEMA_ARG_INDEX);
        if (null == file) {
            logger.error("workflow({}) task({}) invalid args", workflowId, uuid);
            return JobStatus.CLI_ARGS_LOST_SCHEMA;
        }
        logger.info("workflow({}) task({}) fire at: {}", workflowId, uuid, ts);
        return execute(JobSchemaManager.parse(file));
    }

    private JobStatus execute(JobSchema schema) {
        if (null == schema) {
            return JobStatus.CLI_ARGS_EMPTY;
        }
        List<TaskSchema> taskSchemaList = schema.getTasks();
        if (null == taskSchemaList || taskSchemaList.isEmpty()) {
            return JobStatus.JOB_NO_STEP;
        }
        Map<String, String> params = schema.getParams();
        Map<String, Object> jobParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                jobParams.put(entry.getKey(), exprEvaluator.eval(StringUtils.trimToNull(entry.getValue())));
            } catch (Exception e) {
                logger.error("prepare job params({}) error:", entry, e);
                throw new IllegalStateException("prepare job params error", e);
            }
        }
        CompletableFuture[] tasks = taskSchemaList.stream()
                .map((task) -> createTaskFuture(jobParams, params, task))
                .toArray(CompletableFuture[]::new);
        CompletableFuture<JobStatus> future = CompletableFuture.allOf(tasks).thenApply(Void -> {
            logger.info("workflow({}) job({}) all task completed", workflowId, uuid);
            return JobStatus.SUCCESS;
        });
        try {
            return future.get(2, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.info("workflow({}) job({}) execute error", workflowId, uuid, ExceptionUtils.getRootCause(e));
            return JobStatus.FAILURE;
        }
    }

    private CompletableFuture createTaskFuture(Map<String, Object> jobParams, Map<String, String> params, TaskSchema task) {
        logger.info("workflow({}) job({}) task({}) [{}] prepared", workflowId, uuid, UUID.randomUUID(), task.getName());
        return CompletableFuture.supplyAsync(() -> {
                    TaskStepSchema extractorSchema = task.getExtractor();
                    if (null == extractorSchema) {
                        return null;
                    }
                    String extractorType = extractorSchema.getProperty("type");
                    Extractor extractorInstance = Environments.getFeature(Extractor.class, extractorType);
                    if (null == extractorInstance) {
                        throw new IllegalStateException("extract data error, no matched extractor: " + extractorType);
                    } else {
                        logger.info("use [{}] extract data", extractorInstance);
                        Properties stepParams = new Properties();
                        stepParams.putAll(jobParams);
                        stepParams.putAll(extractorSchema.getProperties());
                        return extractorInstance.extract(extractorSchema.getDsl(), stepParams);
                    }
                }, executor)
                .thenApplyAsync(dataframe -> {
                    if (null == dataframe) {
                        return null;
                    }
                    TaskStepSchema[] transformerSchemaArr = task.getTransformers();
                    if (ArrayUtils.isEmpty(transformerSchemaArr)) {
                        return dataframe;
                    }
                    String transformerType;
                    Transformer transformerInstance;
                    for (TaskStepSchema transformerSchema : transformerSchemaArr) {
                        transformerType = transformerSchema.getProperty("type");
                        transformerInstance = Environments.getFeature(Transformer.class, transformerType);
                        if (null == transformerInstance) {
                            throw new IllegalStateException("transform data error, no matched transformer: " + transformerType);
                        } else {
                            Properties stepParams = new Properties();
                            stepParams.putAll(jobParams);
                            stepParams.putAll(transformerSchema.getProperties());
                            logger.info("use [{}] transform data", transformerInstance);
                            dataframe = transformerInstance.transform(transformerSchema.getDsl(), dataframe, stepParams);
                        }
                    }
                    return dataframe;
                }, executor).thenAcceptAsync(dataframe -> {
                    if (null == dataframe) {
                        logger.error("no data to loaded");
                    } else {
                        TaskStepSchema loaderSchema = task.getLoader();
                        String loaderType = loaderSchema.getProperty("type");
                        Loader loaderInstance = Environments.getFeature(Loader.class, loaderType);
                        if (null == loaderInstance) {
                            throw new IllegalStateException("load data error, no matched loader: " + loaderType);
                        } else {
                            Properties stepParams = new Properties();
                            stepParams.putAll(jobParams);
                            stepParams.putAll(loaderSchema.getProperties());
                            logger.info("use [{}] load data", loaderInstance);
                            loaderInstance.load(loaderSchema.getDsl(), dataframe, stepParams);
                        }
                    }
                }, executor);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        new JobLauncher(UUID.randomUUID()).launch(args);
    }
}

