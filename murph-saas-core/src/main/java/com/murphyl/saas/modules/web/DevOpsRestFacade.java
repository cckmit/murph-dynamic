package com.murphyl.saas.modules.web;

import com.murphyl.dynamic.NamedFeature;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * DevOps Rest - Service
 *
 * @author: murph
 * @date: 2021/12/26 - 0:58
 */
public class DevOpsRestFacade extends AbstractVerticle implements NamedFeature {

    private static final Logger logger = LoggerFactory.getLogger(DevOpsRestFacade.class);

    private final Options options;

    @Inject
    public DevOpsRestFacade(Config env) {
        this.options = ConfigBeanFactory.create(env.getConfig(name()), Options.class);
    }

    @Override
    public String name() {
        return "devops";
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        logger.info("正在初始化 DevOps 模块……");
        logger.info("DevOps 模块开放端口：{}", options.port);
    }


    /**
     * 配置选项
     */
    public static class Options {

        private int port;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

}
