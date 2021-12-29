package com.murphyl.saas.modules.web.server;

import com.murphyl.saas.features.WebServerManagerFeature;
import com.murphyl.saas.modules.web.router.devops.DynamicFrontRouterManager;
import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
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
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * DevOps 服务
 *
 * @author: murph
 * @date: 2021/12/26 - 0:58
 */
public class DevOpsWebServer extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DevOpsWebServer.class);

    private static final String SERVER_HOST_KEY = "server.host";

    private static final String PREVIEW_ROOT = "/routes/hot/preview";

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
            logger.warn("Please set DevOps web server host hostname by configuration: {}", SERVER_HOST_KEY);
        } else {
            severOptions.setHost(options.getHost());
        }
        Router root = Router.router(vertx);
        migrate(root);
        httpServer = vertx.createHttpServer(severOptions).requestHandler(root).listen(event -> {
            if (event.succeeded()) {
                logger.info("DevOps HTTP server deploy success: {}", severOptions.toJson());
            } else {
                logger.error("DevOps HTTP server deploy failure", event.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
        if (null == httpServer) {
            logger.warn("HTTP Web 服务器没有初始化？");
            return;
        }
        httpServer.close(ctx -> {
            if (ctx.succeeded()) {
                logger.info("关闭 HTTP Web 服务器成功");
            } else {
                logger.error("关闭 HTTP Web 服务器失败", ctx.cause());
            }
        });
    }

    private void migrate(Router root) {
        root.route().failureHandler(ctx -> {
            logger.info("执行脚本请求出错", ctx.failure());
        });
        // 路由列表
        root.route(HttpMethod.GET, "/routes/hot").handler(this.listFrontRoutes);
        // 重新加载路由
        root.route(HttpMethod.PUT, "/routes/hot").handler(this.reloadFrontRoutes);
        // 重新发布路由
        root.route(HttpMethod.POST, "/routes/hot").handler(this.deployFrontRoutes);
        // 删除临时路由
        root.route(HttpMethod.DELETE, "/routes/hot").handler(this.removeFrontRoutes);
        // 预览路由
        root.route(PREVIEW_ROOT).path("/*").handler(this.previewFrontRoutesBuilder.get());
    }

    private Handler<RoutingContext> listFrontRoutes = (ctx) -> {
        JsonObject response = new JsonObject();
        try {
            Map<String, Value> routeTable = context.get(PREVIEW_ROOT);
            if (null == routeTable || routeTable.isEmpty()) {
                ctx.json(response.put("code", 0).put("message", "nothing here"));
            } else {
                ctx.json(response.put("code", 0).put("payload", dynamicRouterManager.getLoadedRoutes()));
            }
        } catch (Exception e) {
            ctx.json(response.put("code", 1).put("message", "展示前端路由规则失败").put("raw", e.getMessage()));
        }
    };

    private Handler<RoutingContext> reloadFrontRoutes = (ctx) -> {
        try {
            logger.info("Reloading front user route rules……");
            dynamicRouterManager.reload();
            List<RestRoute<Value>> routes = dynamicRouterManager.getLoadedRoutes();
            Map<String, Value> routeTable = new HashMap<>(routes.size());
            for (RestRoute<Value> route : routes) {
                routeTable.put(route.getPath(), route.getFunction());
            }
            context.put(PREVIEW_ROOT, routeTable);
            ctx.redirect(ctx.currentRoute().getPath());
        } catch (Exception e) {
            logger.info("Reload front user route error", e);
            ctx.fail(500, e);
        }
    };

    private Handler<RoutingContext> deployFrontRoutes = (ctx) -> {
        vertx.eventBus().publish(WebServerManagerFeature.EVENT_KEY, WebServerManagerFeature.Events.DEPLOY);
        // 事件发送完成
        ctx.json(new JsonObject().put("code", 0).put("payload", context.remove(PREVIEW_ROOT)).put("message", "event published"));
    };

    private Handler<RoutingContext> removeFrontRoutes = (ctx) -> {
        ctx.json(new JsonObject().put("code", 0).put("payload", context.remove(PREVIEW_ROOT)));
    };

    private Supplier<Handler<RoutingContext>> previewFrontRoutesBuilder = () -> {
        LoggerProxy loggerProxy = new LoggerProxy(LoggerFactory.getLogger("front-user-routes-previewer"));
        return (ctx) -> {
            // 处理请求路径
            String path = StringUtils.removeStart(ctx.request().path(), PREVIEW_ROOT);
            logger.info("Preview route: {}", path);
            // 未开启预览或者预览缓存已删除
            if (null == context.get(PREVIEW_ROOT)) {
                ctx.fail(500);
                return;
            }
            // 请求的规则不存在
            Map<String, Value> routeTable = context.get(PREVIEW_ROOT);
            if (null == routeTable.get(path)) {
                ctx.fail(404);
                return;
            }
            // 标记请求
            HttpServerResponse response = ctx.response();
            response.putHeader("x-dynamic-route", path);
            // 处理请求
            Map<String, Object> arguments = new HashMap<>(2);
            arguments.put("logger", loggerProxy);
            arguments.put("rest", new RestProxy(ctx));
            routeTable.get(path).executeVoid(arguments);
        };
    };

}
