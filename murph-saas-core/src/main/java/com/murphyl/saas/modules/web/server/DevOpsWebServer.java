package com.murphyl.saas.modules.web.server;

import com.murphyl.saas.modules.web.router.devops.DynamicFrontRouterManager;
import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.server.WebServerOptions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * DevOps 服务
 *
 * @author: murph
 * @date: 2021/12/26 - 0:58
 */
public class DevOpsWebServer extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DevOpsWebServer.class);

    private static final String SERVER_HOST_KEY = "server.host";

    @Inject
    private DynamicFrontRouterManager dynamicRouterManager;

    @Inject
    private Config configModule;


    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        WebServerOptions options = ConfigBeanFactory.create(configModule.getConfig("devops"), WebServerOptions.class);
        HttpServerOptions severOptions = new HttpServerOptions().setLogActivity(true).setPort(options.getPort());
        if (StringUtils.isEmpty(options.getHost())) {
            logger.warn("可以通过（）配置项设置 DevOps Web 服务器的 hostname");
        } else {
            severOptions.setHost(options.getHost());
        }
        httpServer = vertx.createHttpServer(severOptions);
        httpServer.requestHandler(buildRouter()).listen(event -> {
            if (event.succeeded()) {
                logger.info("DevOps HTTP server deploy success: {}", severOptions.toJson());
            } else {
                logger.error("DevOps HTTP server deploy failure", event.cause());
            }
        });
    }

    private Router buildRouter() {
        Router router = Router.router(vertx);
        router.route().failureHandler(ctx -> {
            logger.info("执行脚本请求出错", ctx.failure());
        });
        // 路由列表
        router.route(HttpMethod.GET, "/routes").handler(this.listFrontRoutes);
        // 重新加载路由
        router.route(HttpMethod.GET, "/routes/hot").handler(this.reloadFrontRoutes);
        // 重新发布路由
        router.route(HttpMethod.POST, "/routes/hot").handler(this.deployFrontRoutes);
        // 删除临时路由
        router.route(HttpMethod.DELETE, "/routes/hot").handler(this.removeFrontRoutes);
        // 预览路由
        Router previewRouter = Router.router(vertx);
        // 预览根路由
        previewRouter.route("/").handler(ctx -> {
            ctx.json(new JsonObject().put("empty", null));
        });
        router.mountSubRouter("/preview", previewRouter);
        return router;
    }

    private Handler<RoutingContext> listFrontRoutes = (ctx) -> {
        JsonObject response = new JsonObject();
        try {
            List<RestRoute<Value>> routes = dynamicRouterManager.load();
            ctx.json(response.put("code", 0).put("payload", routes));
        } catch (Exception e) {
            ctx.json(response.put("code", 1).put("message", "展示前端路由规则失败").put("raw", e.getMessage()));
        }
    };

    private Handler<RoutingContext> reloadFrontRoutes = (ctx) -> {
        JsonObject response = new JsonObject();
        try {
            ctx.json(response.put("code", 0).put("payload", "reloadFrontRoutes"));
        } catch (Exception e) {
            ctx.json(response.put("code", 0).put("message", "reloadFrontRoutes").put("raw", e.getMessage()));
        }
    };

    private Handler<RoutingContext> deployFrontRoutes = (ctx) -> {
        JsonObject response = new JsonObject();
        try {
            ctx.json(response.put("code", 0).put("payload", "deployFrontRoutes"));
        } catch (Exception e) {
            ctx.json(response.put("code", 0).put("message", "deployFrontRoutes").put("raw", e.getMessage()));
        }
    };

    private Handler<RoutingContext> removeFrontRoutes = (ctx) -> {
        JsonObject response = new JsonObject();
        try {
            ctx.json(response.put("code", 0).put("payload", "removeFrontRoutes"));
        } catch (Exception e) {
            ctx.json(response.put("code", 0).put("message", "removeFrontRoutes").put("raw", e.getMessage()));
        }
    };

}
