package com.murphyl.etl;

import com.murphyl.etl.support.Environments;
import com.murphyl.etl.support.JobStatus;
import com.murphyl.etl.utils.ThreadPoolFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * 工作流 - 执行器
 *
 * @date: 2021/12/1 10:27
 * @author: murph
 */
public final class WorkflowLauncher implements Callable<JobStatus> {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowLauncher.class);

    private final UUID uuid;
    private final String[] args;

    public final ExecutorService executor;

    public WorkflowLauncher(UUID uuid, String... args) {
        this.uuid = uuid;
        this.args = args;
        int corePoolSize = Environments.getInt("WORKFLOW_POOL_CORE_SIZE", 5);
        int maxPoolSize = Environments.getInt("WORKFLOW_POOL_MAX_SIZE", 5);
        int keepAliveMinutes = Environments.getInt("WORKFLOW_POOL_KEEP_ALIVE_MINUTE", 5);
        int queueSize = Environments.getInt("WORKFLOW_POOL_QUEUE_SIZE", 50);
        String poolNamePrefix = Environments.get("WORKFLOW_POOL_NAME_PREFIX", "workflow");
        this.executor = ThreadPoolFactory.create(corePoolSize, maxPoolSize, keepAliveMinutes, queueSize, poolNamePrefix);
    }

    @Override
    public JobStatus call() {
        if (ArrayUtils.isEmpty(args)) {
            logger.error("workflow({}) invalid args: {}", uuid, args);
            return JobStatus.CLI_ARGS_EMPTY;
        }
        String ts = ArrayUtils.get(args, Environments.CLI_TS_ARG_INDEX);
        String[] files = ArrayUtils.remove(args, Environments.CLI_TS_ARG_INDEX);
        if (ArrayUtils.isEmpty(files)) {
            logger.error("workflow({}) missing job schema, workflow args: {}", uuid, Arrays.toString(args));
            return JobStatus.CLI_ARGS_LOST_SCHEMA;
        }
        logger.info("workflow({}) load job schema from files：{}", uuid, Arrays.toString(files));
        try {
            return this.processJobSchemaArray(ts, files);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 处理任务配置数据
     *
     * @param ts
     * @param files
     * @return
     */
    private JobStatus processJobSchemaArray(String ts, String[] files) {
        if (ArrayUtils.isEmpty(files)) {
            return JobStatus.CLI_ARGS_LOST_SCHEMA;
        }
        JobLauncher launcher = new JobLauncher(uuid);
        CompletableFuture[] futures = Stream.of(files).map(file -> {
            String[] taskArgs = new String[]{ts, file};
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return launcher.launch(taskArgs);
                } catch (ExecutionException e) {
                    logger.error("workflow({}) schema({}) execute error", uuid, taskArgs, e);
                    return JobStatus.FAILURE;
                } catch (InterruptedException e) {
                    logger.error("workflow({}) schema({}) interrupt error", uuid, taskArgs, e);
                    return JobStatus.FAILURE;
                } catch (TimeoutException e) {
                    logger.error("workflow({}) schema({}) execute timeout", uuid, taskArgs, e);
                    return JobStatus.FAILURE;
                } finally {
                    logger.info("workflow({}) schema({}) finished", uuid, taskArgs);
                }
            }, executor);
        }).toArray(CompletableFuture[]::new);
        // 收集执行状态
        CompletableFuture<JobStatus> collector = CompletableFuture.allOf(futures).thenApplyAsync((Void) -> {
            logger.info("workflow({}) completed", uuid);
            return JobStatus.SUCCESS;
        }, executor);
        try {
            return collector.get(2, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.error("workflow({}) execute error", uuid, e);
            return JobStatus.FAILURE;
        } finally {
            launcher.shutdown();
            logger.info("workflow({}) all schema processed: {}", uuid, Arrays.toString(files));
        }

    }

    public static void main(String... args) throws ExecutionException, InterruptedException, TimeoutException {
        WorkflowLauncher launcher = new WorkflowLauncher(UUID.randomUUID(), args);
        JobStatus status = launcher.executor.submit(launcher).get(2, TimeUnit.HOURS);
        if (!StringUtils.contains(System.getProperty("sun.java.command"), "junit")) {
            System.exit(status.code);
        }
    }

}
