package com.murphyl.saas;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.murphyl.saas.core.SaasContextModule;
import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.support.VerticleProxy;
import com.typesafe.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.ServiceHelper;
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
        logger.info("应用核心线程池容量：{}", workerPoolSize);
        DeploymentOptions options = new DeploymentOptions()
                .setWorker(true).setWorkerPoolName("saas-feature").setWorkerPoolSize(workerPoolSize);
        // 发布通过 SPI 加载的动态模块
        Collection<SaasFeature> features = ServiceHelper.loadFactories(SaasFeature.class);
        Validate.notEmpty(features, "无法通过 SPI 加载动态模块：" + SaasFeature.class.getCanonicalName());
        for (SaasFeature saasFeature : ServiceHelper.loadFactories(SaasFeature.class)) {
            injector.injectMembers(saasFeature);
            vertx.deployVerticle(new VerticleProxy(saasFeature), options, deployed -> {
                if (deployed.succeeded()) {
                    logger.info("插件（{}）发布完成！", saasFeature);
                } else {
                    logger.info("插件（{}）发布出错", saasFeature, deployed.cause());
                }
            });
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        logger.info("应用退出执行！");
    }
}
