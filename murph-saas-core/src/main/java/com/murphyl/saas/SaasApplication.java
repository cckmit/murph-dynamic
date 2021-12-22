package com.murphyl.saas;

import com.murphyl.saas.core.AppContext;
import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.feature.RestFeature;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * 入口
 *
 * @author: murph
 * @date: 2021/12/22 - 23:43
 */
public class SaasApplication extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SaasApplication.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        /**
        ServiceLoader.load(SaasFeature.class).forEach(saasFeature -> {
            if (AppContext.CONFIG.getBoolean(saasFeature.unique() + ".enable")) {
                logger.info("加载动态插件：{}", saasFeature);
                vertx.deployVerticle(saasFeature);
            } else {
                logger.info("动态插件（{}）被禁用！", saasFeature);
            }
        });
         **/
        vertx.deployVerticle(RestFeature.class, new DeploymentOptions());
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        logger.info("应用退出执行！");
    }
}
