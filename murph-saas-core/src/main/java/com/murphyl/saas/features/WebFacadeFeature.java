package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.modules.web.DevOpsServerFacade;
import com.murphyl.saas.modules.web.PublicServerFacade;
import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
import com.murphyl.saas.support.web.schema.RestRoute;
import com.murphyl.saas.support.web.schema.RouteProfileLoaderBuilder;
import com.murphyl.saas.support.web.schema.manager.RestProfileLoader;
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
public class WebFacadeFeature implements SaasFeature<Vertx> {

    private static final Logger logger = LoggerFactory.getLogger(WebFacadeFeature.class);

    @Inject
    private Config env;

    @Inject
    private PublicServerFacade publicRestFacade;

    @Inject
    private DevOpsServerFacade devOpsRestFacade;

    private static final String SERVER_HOST_KEY = "server.host";
    private static final String SERVER_PORT_KEY = "server.port";

    private RestProfileLoader routeSchemaManager;

    private HttpServer httpServer;

    private Map<String, RestRoute<Value>> routesTable;

    @Inject
    private RouteProfileLoaderBuilder restRouteSchemaManagerBuilder;

    @Override
    public String name() {
        return "rest";
    }

    @Override
    public void init(Vertx vertx) {
        RestProfileLoader routeSchemaManager = restRouteSchemaManagerBuilder.build();
        List<RestRoute> routes = routeSchemaManager.load();
        routesTable = new ConcurrentHashMap<>();
        for (RestRoute<Value> route : routes) {
            routesTable.put(route.getNamespace(), route);
        }
        logger.info("正在启动 Rest 模块……");
        DeploymentOptions options = new DeploymentOptions().setWorker(true).setWorkerPoolName("web-facade");
        vertx.deployVerticle(publicRestFacade, options, dr -> {
            if (dr.succeeded()) {
                logger.info("开放 Rest 服务模块发布完成：{}", dr.result());
            } else {
                logger.error("开放 Rest 服务模块发布失败", dr.cause());
            }
        });
        vertx.deployVerticle(devOpsRestFacade, options, dr -> {
            if (dr.succeeded()) {
                logger.info("DevOps 服务模块发布完成：{}", dr.result());
            } else {
                logger.error("DevOps 服务模块发布失败", dr.cause());
            }
        });
        // server
        httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        // favicon
        router.route().order(0).handler(new FaviconHandlerImpl(vertx)).failureHandler(ctx -> {
            logger.info("请求出错", ctx.failure());
            ctx.json(new JsonObject().put("message", ctx.failure().getMessage()));
        });
        // root
        router.mountSubRouter("/", home(vertx));
        // dash
        router.mountSubRouter("/_/devops", devops(vertx));
        // metrics
        router.mountSubRouter("/_/metrics", metrics(vertx));
        // socket address
        int port = env.getInt(SERVER_PORT_KEY);
        SocketAddress address;
        if (env.hasPath(SERVER_HOST_KEY)) {
            address = new SocketAddressImpl(port, env.getString(SERVER_HOST_KEY));
        } else {
            logger.warn("可以通过（{}）设置服务发布的域名", SERVER_HOST_KEY);
            address = new SocketAddressImpl(new InetSocketAddress(port));
        }
        // start server
        httpServer.requestHandler(router).listen(address, result -> {
            if (result.succeeded()) {
                logger.info("Rest 服务成功发布：{}", address);
            } else {
                logger.error("Rest 模块启动出错", result.cause());
            }
        });
    }

    private Router home(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.json("home"));
        for (RestRoute<Value> rule : routesTable.values()) {
            Route route = router.route(rule.getPath()).putMetadata("namespace", rule.getNamespace());
            if (null != rule.getMethod()) {
                route.method(HttpMethod.valueOf(rule.getMethod()));
            }
            route.handler(ctx -> {
                Logger logger = LoggerFactory.getLogger(rule.getNamespace());
                RestRoute<Value> current = routesTable.get(ctx.currentRoute().getMetadata("namespace"));
                if (null == current) {
                    ctx.end("no dynamic function");
                } else {
                    current.getFunction().executeVoid(createArguments(logger, ctx));
                }
            });
        }
        return router;
    }

    private Router devops(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route("/routes")
                .method(HttpMethod.GET).handler((ctx) -> {
                    ctx.json(routesTable.values());
                })
                .method(HttpMethod.POST).handler(ctx -> {
                    ctx.end("ADDED");
                });
        Map<String, RestRoute> previewTable = new ConcurrentHashMap<>();
        router.route(HttpMethod.GET, "/routes/hot").handler(ctx -> {
            logger.info("正在重新加载路由配置……");
            List<RestRoute> loaded = routeSchemaManager.load();
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
                    previewRule.getFunction().executeVoid(createArguments(logger, ctx));
                }
            });
        }
        router.mountSubRouter("/preview", previewRouter);
        router.route()
                .failureHandler(ctx -> {
                    logger.info("执行脚本请求出错", ctx.failure());
                });
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("dash"));
        return router;
    }

    private Router metrics(Vertx vertx) {
        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("metrics"));
        return router;
    }

    private Map<String, Object> createArguments(Logger logger, RoutingContext context) {
        Map<String, Object> result = new HashMap<>(2);
        result.put("logger", new LoggerProxy(logger));
        result.put("rest", new RestProxy(context));
        return result;
    }

}
