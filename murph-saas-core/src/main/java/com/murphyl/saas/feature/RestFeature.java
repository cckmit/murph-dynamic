package com.murphyl.saas.feature;

import com.murphyl.saas.core.SaasFeature;
import com.murphyl.saas.support.DynamicModule;
import com.murphyl.saas.support.Environments;
import com.murphyl.saas.support.expression.JavaScriptSupport;
import com.murphyl.saas.support.feature.rest.RestProxy;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.impl.FaviconHandlerImpl;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

    private Map<String, String> DEFAULT_HEADERS = new TreeMap<>();

    public RestFeature() {
        DEFAULT_HEADERS.put("X-APP-NAME", Environments.getString("app.name"));
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
        router.route().handler(new FaviconHandlerImpl(vertx));
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
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("home"));
        return router;
    }

    private Router dash() {
        String src = "export { default } from 'test.home.js';";
        Router router = Router.router(vertx);
        Map<String, Object> params = new HashMap<>(2);
        params.put("logger", logger);
        Value exports = JavaScriptSupport.eval(JavaScriptSupport.createSource(src), params);
        router.get("/hello").handler((ctx) -> {
            if (exports.hasMember("default") && exports.getMember("default").canExecute()) {
                exports.getMember("default").executeVoid(new RestProxy(ctx));
            } else {
                ctx.end("TODO");
            }
        });
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("dash"));
        return router;
    }

    private Router metrics() {
        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("metrics"));
        return router;
    }

}
