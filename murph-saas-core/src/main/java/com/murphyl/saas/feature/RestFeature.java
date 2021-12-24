package com.murphyl.saas.feature;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.support.DynamicModule;
import com.murphyl.saas.support.Environments;
import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
import com.murphyl.saas.support.rest.schema.RestRoute;
import com.murphyl.saas.support.rest.schema.loader.RestProfileLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
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

    private HttpServer httpServer;

    public RestFeature() {

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
        if (DynamicModule.REST_DASH.enabled()) {
            router.mountSubRouter("/_/dash", dash());
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
        List<RestRoute> routes = RestProfileLoader.getInstance().load();
        for (RestRoute rule : routes) {
            Route route = router.route(rule.getPath());
            if (null != rule.getMethod()) {
                route.method(rule.getMethod());
            }
            route.handler(ctx -> {
                Logger logger = LoggerFactory.getLogger(rule.getNamespace());
                rule.getFunction().executeVoid(createArguments(logger, ctx));
            });
        }
        return router;
    }

    private Router dash() {
        Router router = Router.router(vertx);
        router.get("/hello").handler((ctx) -> {
            ctx.end("TODO");
        }).failureHandler(ctx -> {
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
