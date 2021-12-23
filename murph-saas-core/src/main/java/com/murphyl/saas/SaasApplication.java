package com.murphyl.saas;

import com.murphyl.saas.support.Environments;
import com.murphyl.saas.core.SaasFeature;
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
        // 禁用 GraalJS 的告警日志
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        // 初始化动态特征执行环境
        int workerPoolSize = Environments.getInt("app.pool.size");
        logger.info("应用核心线程池容量：{}", workerPoolSize);
        DeploymentOptions options = new DeploymentOptions();
        options.setWorkerPoolName("app-feature").setWorkerPoolSize(workerPoolSize);
        // 发布通过 SPI 加载的动态模块
        ServiceLoader.load(SaasFeature.class).forEach(saasFeature -> {
            vertx.deployVerticle(saasFeature, options, result -> {
                if (result.succeeded()) {
                    logger.info("插件（{}）发布完成！", saasFeature);
                } else {
                    logger.info("插件（{}）发布出错", saasFeature, result.cause());
                }
            });
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        logger.info("应用退出执行！");
    }
}
