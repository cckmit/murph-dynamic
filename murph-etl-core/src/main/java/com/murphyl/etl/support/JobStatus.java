package com.murphyl.etl.support;

/**
 * 任务 - 状态码 - https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
 *
 * @date: 2021/12/1 10:59
 * @author: murph
 */
public enum JobStatus {


    SUCCESS(200),
    ACCEPTED(201),
    NO_CONTENT(204),
    FAILURE(500),
    NOT_IMPLEMENTED(501),
    TIMEOUT(504)
    ;

    public final int code;

    JobStatus(int code) {
        this.code = code;
    }

}
