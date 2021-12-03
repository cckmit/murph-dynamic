package com.murphyl.etl;

import com.google.common.collect.Maps;
import com.murphyl.etl.job.JobSchema;
import com.murphyl.etl.schema.JobSchemaManager;
import com.murphyl.etl.support.Environments;
import com.murphyl.etl.support.JobStatus;
import com.murphyl.etl.task.TaskSchema;
import com.murphyl.etl.task.TaskStepSchema;
import com.murphyl.etl.task.children.TaskChildrenManager;
import com.murphyl.etl.task.children.extractor.Extractor;
import com.murphyl.etl.task.children.loader.Loader;
import com.murphyl.etl.task.children.transformer.Transformer;
import com.murphyl.etl.utils.ThreadPoolFactory;
import com.murphyl.expr.core.ExpressionEvaluator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * 任务 - 执行器
 *
 * @date: 2021/12/1 14:10
 * @author: murph
 */
public final class JobLauncher implements Callable<JobStatus> {

    private static final Logger logger = LoggerFactory.getLogger(JobLauncher.class);

    protected static final ExecutorService EXECUTOR = ThreadPoolFactory.create(15, 35, 10, 30000, "job");

    private static final String EXPRESSION_RESOLVER = "jexl";

    private static ExpressionEvaluator exprEvaluator;

    static {
        exprEvaluator = Environments.getExprEvaluator(EXPRESSION_RESOLVER);
    }

    private UUID uuid;
    private UUID workflow;
    private String[] args;

    public JobLauncher(UUID uuid, String... args) {
        this(UUID.randomUUID(), uuid, args);
    }

    protected JobLauncher(UUID workflow, UUID uuid, String... args) {
        this.workflow = workflow;
        this.uuid = uuid;
        this.args = args;
    }

    @Override
    public JobStatus call() {
        if (ArrayUtils.isEmpty(args)) {
            logger.error("workflow({}) task({}) invalid args", workflow, uuid);
            return JobStatus.CLI_ARGS_EMPTY;
        }
        String ts = ArrayUtils.get(args, Environments.CLI_TS_ARG_INDEX);
        String file = ArrayUtils.get(args, Environments.JOB_SCHEMA_ARG_INDEX);
        if (null == file) {
            logger.error("workflow({}) task({}) invalid args", workflow, uuid);
            return JobStatus.CLI_ARGS_LOST_SCHEMA;
        }
        logger.info("workflow({}) task({}) fire at: {}", workflow, uuid, ts);
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
        CompletableFuture[] tasks = taskSchemaList.stream()
                .map((task) -> createTaskFuture(params, task))
                .toArray(CompletableFuture[]::new);
        CompletableFuture<JobStatus> future = CompletableFuture.allOf(tasks).thenApply(Void -> {
            logger.info("workflow({}) job({}) all task completed", workflow, uuid);
            return JobStatus.SUCCESS;
        });
        try {
            return future.get(2, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.info("workflow({}) job({}) execute error", ExceptionUtils.getRootCause(e));
            return JobStatus.FAILURE;
        }
    }

    private CompletableFuture createTaskFuture(Map<String, String> params, TaskSchema task) {
        logger.info("workflow({}) job({}) task({}) [{}] prepared", workflow, uuid, UUID.randomUUID(), task.getName());
        Map<String, Object> jobParams = Maps.newHashMap();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            jobParams.put(entry.getKey(), exprEvaluator.eval(StringUtils.trimToNull(entry.getValue())));
        }
        return CompletableFuture.supplyAsync(() -> {
                    TaskStepSchema extractorSchema = task.getExtractor();
                    if (null == extractorSchema) {
                        return null;
                    }
                    String extractorType = extractorSchema.getProperty("type");
                    Extractor extractorInstance = TaskChildrenManager.getExtractor(extractorType);
                    if (null == extractorInstance) {
                        throw new IllegalStateException("extract data error, no matched extractor: " + extractorType);
                    } else {
                        logger.info("use [{}] extract data", extractorInstance);
                        return extractorInstance.extract(extractorSchema.getDsl(), jobParams, extractorSchema.getProperties());
                    }
                }, EXECUTOR)
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
                        transformerInstance = TaskChildrenManager.getTransformer(transformerType);
                        if (null == transformerInstance) {
                            throw new IllegalStateException("transform data error, no matched transformer: " + transformerType);
                        } else {
                            Properties props = transformerSchema.getProperties();
                            logger.info("use [{}] transform data: {}", transformerInstance, props);
                            dataframe = transformerInstance.transform(jobParams, dataframe, props);
                        }
                    }
                    return dataframe;
                }, EXECUTOR).thenAcceptAsync(dataframe -> {
                    if (null == dataframe) {
                        logger.error("no data to loaded");
                    } else {
                        TaskStepSchema loaderSchema = task.getLoader();
                        String loaderType = loaderSchema.getProperty("type");
                        Loader loaderInstance = TaskChildrenManager.getLoader(loaderType);
                        if (null == loaderInstance) {
                            throw new IllegalStateException("load data error, no matched loader: " + loaderType);
                        } else {
                            logger.info("use [{}] load data", loaderInstance);
                            loaderInstance.load(jobParams, dataframe, loaderSchema.getProperties());
                        }
                    }
                }, EXECUTOR);
    }
}

