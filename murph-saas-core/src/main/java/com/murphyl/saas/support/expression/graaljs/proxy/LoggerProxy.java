package com.murphyl.saas.support.expression.graaljs.proxy;

import org.graalvm.polyglot.HostAccess;
import org.slf4j.Logger;

/**
 * 日志 - 桥接对象
 *
 * @date: 2021/12/24 13:59
 * @author: murph
 */
@HostAccess.Implementable
public class LoggerProxy {

    private final  Logger logger;

    public LoggerProxy(Logger logger) {
        this.logger = logger;
    }

    @HostAccess.Export
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

}
