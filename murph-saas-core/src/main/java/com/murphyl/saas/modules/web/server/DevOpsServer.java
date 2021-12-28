package com.murphyl.saas.modules.web.server;

import com.murphyl.saas.modules.web.router.devops.DynamicUserRouterManager;
import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;
import com.murphyl.saas.support.web.server.WebServerHelper;
import com.murphyl.saas.support.web.server.WebServerOptions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DevOps 服务
 *
 * @author: murph
 * @date: 2021/12/26 - 0:58
 */
public class DevOpsServer extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DevOpsServer.class);

    @Inject
    private DynamicUserRouterManager dynamicRouterManager;

    @Inject
    private RouteProfileLoader routeProfileLoader;

    private WebServerOptions options;

    private HttpServer httpServer;

    private Map<String, RestRoute<Value>> routesTable;

    @Inject
    public DevOpsServer(Config env) {
        this.options = ConfigBeanFactory.create(env.getConfig("devops"), WebServerOptions.class);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        List<RestRoute> routes = routeProfileLoader.load();
        routesTable = new ConcurrentHashMap<>();
        for (RestRoute<Value> route : routes) {
            routesTable.put(route.getNamespace(), route);
        }
        httpServer = vertx.createHttpServer();
        SocketAddress address = WebServerHelper.createAddress(options);
        httpServer.requestHandler(buildRouter()).listen(address, event -> {
            if (event.succeeded()) {
                logger.info("DevOps HTTP 服务模块发布完成：{}", address);
            } else {
                logger.error("DevOps HTTP 服务发布失败", event.cause());
            }
        });
    }


    private Router buildRouter() {
        Router router = Router.router(vertx);
        router.route().failureHandler(ctx -> {
            logger.info("执行脚本请求出错", ctx.failure());
        });
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("dash"));
        router.route(HttpMethod.GET, "/routes").handler((ctx) -> {
            ctx.json(routesTable.values());
        });
        router.route(HttpMethod.POST, "/routes").handler(ctx -> {
            ctx.end("ADDED");
        });
        Map<String, RestRoute> previewTable = new ConcurrentHashMap<>();
        router.route(HttpMethod.GET, "/routes/hot").handler(ctx -> {
            logger.info("正在重新加载路由配置……");
            List<RestRoute> loaded = routeProfileLoader.load();
            for (RestRoute route : loaded) {
                previewTable.put(route.getNamespace(), route);

            }
            ctx.json(loaded);
        });
        router.route(HttpMethod.POST, "/routes/hot").handler(ctx -> {
            for (Map.Entry<String, RestRoute> entry : previewTable.entrySet()) {
                if (routesTable.containsKey(entry.getKey())) {
                    routesTable.replace(entry.getKey(), entry.getValue());
                    logger.info("动态接口[{}]已重新发布", entry.getKey());
                }
            }
            previewTable.clear();
            ctx.end("OK");
        });
        router.route(HttpMethod.DELETE, "/routes/hot").handler(ctx -> {
            previewTable.clear();
            ctx.end("OK");
        });
        Router previewRouter = Router.router(vertx);
        previewRouter.route("/").handler(ctx -> {
            ctx.json(new JsonObject().put("empty", previewTable.isEmpty()).put("routes", previewTable.values()));
        });
        for (Map.Entry<String, RestRoute<Value>> entry : routesTable.entrySet()) {
            RestRoute<Value> rule = entry.getValue();
            Route route = previewRouter.route(rule.getPath()).putMetadata("namespace", entry.getKey());
            if (null != rule.getMethod()) {
                route.method(HttpMethod.valueOf(rule.getMethod()));
            }
            route.handler(ctx -> {
                RestRoute<Value> previewRule = previewTable.get(ctx.currentRoute().getMetadata("namespace"));
                if (null == previewRule) {
                    ctx.end("no preview route");
                } else {
                    Logger logger = LoggerFactory.getLogger(previewRule.getNamespace() + "&preview=true");
                    previewRule.getFunction().executeVoid(WebServerHelper.createArguments(logger, ctx));
                }
            });
        }
        router.mountSubRouter("/preview", previewRouter);
        return router;
    }

}
