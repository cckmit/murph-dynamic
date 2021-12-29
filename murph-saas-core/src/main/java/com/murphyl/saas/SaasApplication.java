package com.murphyl.saas;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.murphyl.saas.core.SaasContextModule;
import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.support.VerticleProxy;
import com.typesafe.config.Config;
import io.vertx.core.*;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * 入口
 *
 * @author: murph
 * @date: 2021/12/22 - 23:43
 */
public class SaasApplication extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(SaasApplication.class);

    static {
        // 禁用 GraalJS 的告警日志
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        Injector injector = Guice.createInjector(new SaasContextModule());
        Config env = injector.getInstance(Config.class);
        // 初始化动态特征执行环境
        int workerPoolSize = env.getInt("app.pool.size");
        logger.info("Vert.x core poll size：{}", workerPoolSize);
        DeploymentOptions options = new DeploymentOptions()
                .setWorker(true).setWorkerPoolName("saas-feature").setWorkerPoolSize(workerPoolSize);
        // 发布通过 SPI 加载的动态模块
        Collection<SaasFeature> features = ServiceHelper.loadFactories(SaasFeature.class);
        Validate.notEmpty(features, "Can not load dynamic modules via SPI: " + SaasFeature.class.getCanonicalName());
        for (SaasFeature saasFeature : ServiceHelper.loadFactories(SaasFeature.class)) {
            injector.injectMembers(saasFeature);
            vertx.deployVerticle(new VerticleProxy(saasFeature), options, deployed -> {
                if (deployed.succeeded()) {
                    logger.info("Dynamic module（{}）deploy success！", saasFeature);
                } else {
                    logger.info("Dynamic module（{}）deploy failure", saasFeature, deployed.cause());
                }
            });
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        logger.info("Application exited！");
    }
}
