package com.murphyl.saas.feature;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.support.DynamicModule;
import com.murphyl.saas.support.Environments;
import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
import com.murphyl.saas.support.rest.schema.RestRoute;
import com.murphyl.saas.support.rest.schema.loader.RestRouteSchemaManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.FaviconHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/22 - 23:49
 */
public class RestFeature extends AbstractVerticle implements SaasFeature {

    private static final Logger logger = LoggerFactory.getLogger(RestFeature.class);

    private static final String SERVER_HOST_KEY = "server.host";
    private static final String SERVER_PORT_KEY = "server.port";

    private final RestRouteSchemaManager routeSchemaManager;

    private HttpServer httpServer;

    private final Map<String, RestRoute> ROUTES_TABLE;

    public RestFeature() {
        routeSchemaManager = RestRouteSchemaManager.getInstance();
        List<RestRoute> routes = routeSchemaManager.load();
        ROUTES_TABLE = new ConcurrentHashMap<>();
        for (RestRoute route : routes) {
            ROUTES_TABLE.put(route.getNamespace(), route);
        }
    }

    @Override
    public String unique() {
        return "rest";
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        logger.info("正在启动 Rest 模块……");
        // server
        httpServer = getVertx().createHttpServer();
        Router router = Router.router(vertx);
        // favicon
        router.route().order(0).handler(new FaviconHandlerImpl(vertx)).failureHandler(ctx -> {
            logger.info("请求出错", ctx.failure());
            ctx.end("error");
        });
        // root
        router.mountSubRouter("/", home());
        // dash
        if (DynamicModule.REST_DEVOPS.enabled()) {
            router.mountSubRouter("/_/devops", devops());
        }
        // metrics
        if (DynamicModule.REST_METRICS.enabled()) {
            router.mountSubRouter("/_/metrics", metrics());
        }
        // socket address
        int port = Environments.getInt(SERVER_PORT_KEY);
        SocketAddress address;
        if (Environments.exists(SERVER_HOST_KEY)) {
            address = new SocketAddressImpl(port, Environments.getString(SERVER_HOST_KEY));
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

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        logger.info("尝试卸载 Rest 模块!");
        httpServer.close((Void) -> {
            logger.info("Rest 模块已卸载！");
        });
    }

    private Router home() {
        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.json("home"));
        for (RestRoute rule : ROUTES_TABLE.values()) {
            Route route = router.route(rule.getPath()).putMetadata("namespace", rule.getNamespace());
            if (null != rule.getMethod()) {
                route.method(HttpMethod.valueOf(rule.getMethod()));
            }
            route.handler(ctx -> {
                Logger logger = LoggerFactory.getLogger(rule.getNamespace());
                RestRoute current = ROUTES_TABLE.get(ctx.currentRoute().getMetadata("namespace"));
                if (null == current) {
                    ctx.end("no dynamic function");
                } else {
                    current.getFunction().executeVoid(createArguments(logger, ctx));
                }
            });
        }
        return router;
    }

    private Router devops() {
        Router router = Router.router(vertx);
        router.route("/routes")
                .method(HttpMethod.GET).handler((ctx) -> {
                    ctx.json(ROUTES_TABLE.values());
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
                if (ROUTES_TABLE.containsKey(entry.getKey())) {
                    ROUTES_TABLE.replace(entry.getKey(), entry.getValue());
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
        for (Map.Entry<String, RestRoute> entry : ROUTES_TABLE.entrySet()) {
            RestRoute rule = entry.getValue();
            Route route = previewRouter.route(rule.getPath()).putMetadata("namespace", entry.getKey());
            if (null != rule.getMethod()) {
                route.method(HttpMethod.valueOf(rule.getMethod()));
            }
            route.handler(ctx -> {
                RestRoute previewRule = previewTable.get(ctx.currentRoute().getMetadata("namespace"));
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

    private Router metrics() {
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
