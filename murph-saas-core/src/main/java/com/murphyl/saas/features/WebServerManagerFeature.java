package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.modules.web.server.DevOpsWebServer;
import com.murphyl.saas.modules.web.server.FrontEndWebServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Web 门面 - 特征
 *
 * @author: murph
 * @date: 2021/12/22 - 23:49
 */
public class WebServerManagerFeature implements SaasFeature<Vertx> {

    private static final Logger logger = LoggerFactory.getLogger(WebServerManagerFeature.class);

    @Inject
    private FrontEndWebServer publicRestFacade;

    @Inject
    private DevOpsWebServer devOpsRestFacade;

    @Override
    public String name() {
        return "rest";
    }

    @Override
    public void init(Vertx vertx) {
        logger.info("正在启动 Rest 模块……");
        DeploymentOptions options = new DeploymentOptions().setWorker(true).setWorkerPoolName("http-router");
        vertx.deployVerticle(publicRestFacade, options, deployed -> {
            if (deployed.succeeded()) {
                logger.info("开放 Rest 服务模块发布完成：{}", deployed.result());
            } else {
                logger.error("开放 Rest 服务模块发布失败", deployed.cause());
            }
        });
        vertx.deployVerticle(devOpsRestFacade, options, deployed -> {
            if (deployed.succeeded()) {
                logger.info("DevOps 服务模块发布完成：{}", deployed.result());
            } else {
                logger.error("DevOps 服务模块发布失败", deployed.cause());
            }
        });
    }

}
