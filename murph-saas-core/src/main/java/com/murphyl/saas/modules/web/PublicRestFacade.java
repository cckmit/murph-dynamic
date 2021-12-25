package com.murphyl.saas.modules.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 开放 Rest - 服务
 *
 * @author: murph
 * @date: 2021/12/26 - 0:58
 */
public class PublicRestFacade extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(PublicRestFacade.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        logger.info("正在初始化开放 Rest 模块……");
    }

    class Options {

    }

}
