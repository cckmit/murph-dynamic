package com.murphyl.saas.modules.web.server;

import com.murphyl.saas.modules.web.router.devops.DynamicFrontRouterManager;
import com.murphyl.saas.support.resource.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.resource.graaljs.proxy.RestProxy;
import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.server.WebServerOptions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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

    public static final String DYNAMIC_ROOT = "/dynamic";

    @Inject
    private DynamicFrontRouterManager dynamicUserRouterManager;

    private WebServerOptions options;

    private final Map<String, Value> routeTable = new ConcurrentHashMap<>();

    @Inject
    public FrontEndWebServer(Config env) {
        this.options = ConfigBeanFactory.create(env.getConfig("rest"), WebServerOptions.class);

    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        HttpServerOptions severOptions = new HttpServerOptions().setLogActivity(true).setPort(options.getPort());
        this.reloadRouteTable();
        HttpServer httpServer = vertx.createHttpServer(severOptions);
        Router router = Router.router(vertx);
        this.migrate(router);
        httpServer.requestHandler(router).listen(event -> {
            if (event.succeeded()) {
                logger.info("用户开放 HTTP 服务模块发布完成：{}", severOptions.toJson());
            } else {
                logger.error("用户开放 HTTP 服务发布失败", event.cause());
            }
        });
    }


    private void migrate(Router root) {
        root.route(HttpMethod.GET, "/").handler(ctx -> ctx.end("home"));
        LoggerProxy loggerProxy = new LoggerProxy(LoggerFactory.getLogger("dynamic-function"));
        root.route(DYNAMIC_ROOT).path("/*").handler(ctx -> {
            // 处理请求路径
            String path = StringUtils.removeStart(ctx.request().path(), DYNAMIC_ROOT);
            // 请求的规则不存在
            if (null == routeTable.get(path)) {
                ctx.fail(404);
                return;
            }
            // 标记请求
            HttpServerResponse response = ctx.response();
            response.putHeader("x-function-route", path);
            // 处理请求
            Map<String, Object> arguments = new HashMap<>(2);
            arguments.put("logger", loggerProxy);
            arguments.put("rest", new RestProxy(ctx));
            routeTable.get(path).executeVoid(arguments);
        }).failureHandler(failure -> {
            logger.error("请求处理错误", failure.failure());
            HttpServerResponse response = failure.response();
            response.setStatusCode(failure.statusCode());
            response.putHeader("x-function-error", failure.response().getStatusMessage());
            failure.json(new JsonObject().put("code", 1).put("message", "动态请求执行错误"));
        });
    }

    private synchronized void reloadRouteTable() {
        List<RestRoute<Value>> routes = dynamicUserRouterManager.getLoadedRoutes();
        // 构造临时路由表
        Map<String, Value> tempRouteTable = new HashMap<>(routes.size());
        for (RestRoute<Value> route : routes) {
            tempRouteTable.put(route.getPath(), route.getFunction());
        }
        // 清理路由表
        this.routeTable.clear();
        // 重载路由表
        this.routeTable.putAll(tempRouteTable);
    }

}
