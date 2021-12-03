package com.murphyl.etl.support;

/**
 * 任务 - 状态码 - https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
 *
 * @date: 2021/12/1 10:59
 * @author: murph
 */
public enum JobStatus {

    SUCCESS(0),
    FAILURE(1),
    CLI_ARGS_EMPTY(1),
    CLI_ARGS_LOST_SCHEMA(2),

    JOB_NO_STEP(10),
    ;

    public final int code;

    JobStatus(int code) {
        this.code = code;
    }

}
