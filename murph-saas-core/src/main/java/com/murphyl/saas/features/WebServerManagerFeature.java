package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.modules.web.server.DevOpsWebServer;
import com.murphyl.saas.modules.web.server.FrontEndWebServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
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

    public static final String EVENT_KEY = "web-server-event";

    @Inject
    private FrontEndWebServer publicRestFacade;

    @Inject
    private DevOpsWebServer devOpsRestFacade;

    @Override
    public void init(Vertx vertx) {
        logger.info("Starting web server……");
        this.deployFrontUserServer(vertx);
        this.deployDevOpsServer(vertx);
        this.bindDevOpsEvents(vertx);
    }

    private void deployFrontUserServer(Vertx vertx) {
        DeploymentOptions devopsOptions = new DeploymentOptions().setWorker(true).setWorkerPoolName("public-http-server");
        vertx.deployVerticle(publicRestFacade, devopsOptions, deployed -> {
            if (deployed.succeeded()) {
                logger.info("Public HTTP server deploy success: {}", deployed.result());
            } else {
                logger.error("Public HTTP server deploy failure", deployed.cause());
            }
        });
    }

    private void deployDevOpsServer(Vertx vertx) {
        DeploymentOptions devopsOptions = new DeploymentOptions().setWorker(true).setWorkerPoolName("devops-http-server");
        vertx.deployVerticle(devOpsRestFacade, devopsOptions, deployed -> {
            if (deployed.succeeded()) {
                logger.info("DevOps HTTP server deploy success: {}", deployed.result());
            } else {
                logger.error("DevOps HTTP server deploy failure", deployed.cause());
            }
        });
    }

    private void bindDevOpsEvents(Vertx vertx) {
        vertx.eventBus().consumer(EVENT_KEY, (Message<String> event) -> {
            switch (event.body()) {
                case Events.DEPLOY:
                    vertx.undeploy(publicRestFacade.deploymentID(), undeploy -> {
                        if (undeploy.succeeded()) {
                            logger.info("重新发布前端用户开放 Web 服务器");
                            deployDevOpsServer(vertx);
                        } else {
                            logger.error("重新发布路由规则失败，卸载路由规则出错", undeploy.cause());
                        }
                    });
                    break;
                default:
                    logger.warn("暂不支持的路由规则");
            }
        });
    }

    public class Events {

        public static final String DEPLOY = "reload-front-user-routes";

    }

}
