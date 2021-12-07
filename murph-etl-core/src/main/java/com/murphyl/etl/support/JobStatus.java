package com.murphyl.etl.support;

/**
 * 任务 - 状态码 - https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
 *
 * @date: 2021/12/1 10:59
 * @author: murph
 */
public enum JobStatus {

    FAILURE(0),
    SUCCESS(1),
    SUBMITTED(2),
    ;

    public final int code;

    JobStatus(int code) {
        this.code = code;
    }

}
