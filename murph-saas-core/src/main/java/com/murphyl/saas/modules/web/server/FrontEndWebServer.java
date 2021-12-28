package com.murphyl.saas.modules.web.server;

import com.murphyl.saas.modules.web.router.devops.DynamicFrontRouterManager;
import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;
import com.murphyl.saas.support.web.server.WebServerOptions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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
 * 前端开放 - 服务
 *
 * @author: murph
 * @date: 2021/12/26 - 0:58
 */
public class FrontEndWebServer extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(FrontEndWebServer.class);

    private static final String SERVER_HOST_KEY = "server.host";

    @Inject
    private DynamicFrontRouterManager dynamicUserRouterManager;

    @Inject
    private RouteProfileLoader routeProfileLoader;

    private WebServerOptions options;

    private HttpServer httpServer;

    private Map<String, RestRoute<Value>> routesTable;

    @Inject
    public FrontEndWebServer(Config env) {
        this.options = ConfigBeanFactory.create(env.getConfig("rest"), WebServerOptions.class);
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
        SocketAddress address = createAddress(options);
        httpServer.requestHandler(buildRouter()).listen(address, event -> {
            if (event.succeeded()) {
                logger.info("用户开放 HTTP 服务模块发布完成：{}", address);
            } else {
                logger.error("用户开放 HTTP 服务发布失败", event.cause());
            }
        });
    }


    private Router buildRouter() {
        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("home"));
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

    public static SocketAddress createAddress(WebServerOptions options) {
        if (null == options.getHost()) {
            logger.warn("可以通过（{}）设置服务发布的域名", SERVER_HOST_KEY);
            return new SocketAddressImpl(new InetSocketAddress(options.getPort()));
        } else {
            return new SocketAddressImpl(options.getPort(), options.getHost());
        }
    }

    public static Map<String, Object> createArguments(Logger logger, RoutingContext context) {
        Map<String, Object> result = new HashMap<>(2);
        result.put("logger", new LoggerProxy(logger));
        result.put("rest", new RestProxy(context));
        return result;
    }

}
