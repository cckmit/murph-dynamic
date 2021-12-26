package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.modules.web.server.DevOpsServer;
import com.murphyl.saas.modules.web.server.OpenUserServer;
import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;
import com.typesafe.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.FaviconHandlerImpl;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Web 门面 - 特征
 *
 * @author: murph
 * @date: 2021/12/22 - 23:49
 */
public class WebServerManagerFeature implements SaasFeature<Vertx> {

    private static final Logger logger = LoggerFactory.getLogger(WebServerManagerFeature.class);

    @Inject
    private OpenUserServer publicRestFacade;

    @Inject
    private DevOpsServer devOpsRestFacade;

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
