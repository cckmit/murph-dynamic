package com.murphyl.etl;

import com.murphyl.etl.consts.Environments;
import com.murphyl.etl.consts.JobStatus;
import com.murphyl.etl.utils.ThreadPoolFactory;
import org.apache.commons.lang3.ArrayUtils;
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

    public static final ExecutorService EXECUTOR = ThreadPoolFactory.create(5, 15, 20, 200, "workflow");

    private UUID uuid;
    private String[] args;

    public WorkflowLauncher(UUID uuid, String... args) {
        this.uuid = uuid;
        this.args = args;
    }

    @Override
    public JobStatus call() {
        if (ArrayUtils.isEmpty(args)) {
            logger.error("workflow({}) invalid args", uuid);
            return JobStatus.CLI_ARGS_EMPTY;
        }
        String ts = ArrayUtils.get(args, Environments.CLI_TS_ARG_INDEX);
        String[] files = ArrayUtils.remove(args, Environments.CLI_TS_ARG_INDEX);
        if (ArrayUtils.isEmpty(files)) {
            logger.error("workflow({}) missing job schema, workflow args: {}", uuid, Arrays.toString(args));
            return JobStatus.CLI_ARGS_LOST_SCHEMA;
        }
        logger.info("workflow({}) load job schema from files：{}", uuid, Arrays.toString(files));
        JobStatus result = this.processJobSchemaArray(ts, files);
        logger.info("workflow({}) completed：{}", uuid, result);
        return result;
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
        CountDownLatch latch = new CountDownLatch(files.length);
        CompletableFuture[] futures = Stream.of(files).map(file -> {
            UUID jobId = UUID.randomUUID();
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return new JobLauncher(uuid, jobId, new String[]{ts, file}).call();
                } finally {
                    logger.info("workflow({}) job({}) schema({}) submitted", uuid, jobId, file);
                    latch.countDown();
                }
            }, JobLauncher.EXECUTOR);
        }).toArray(CompletableFuture[]::new);
        // 收集执行状态
        CompletableFuture<JobStatus> collector = CompletableFuture.allOf(futures).thenApplyAsync((Void) -> {
            logger.info("workflow({}) completed", uuid);
            return JobStatus.SUCCESS;
        }, EXECUTOR);
        JobStatus result;
        try {
            latch.await();
            result = collector.get(2, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.error("workflow({}) execute error", uuid, e);
            result = JobStatus.FAILURE;
        } finally {
            logger.info("workflow({}) all schema processed: {} - {}", uuid, ts, Arrays.toString(files));
        }
        return result;
    }

}
