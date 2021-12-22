package com.murphyl.saas.feature;

import com.murphyl.saas.core.AppContext;
import com.murphyl.saas.core.SaasFeature;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/22 - 23:49
 */
public class RestFeature extends AbstractVerticle implements SaasFeature {

    private static final Logger logger = LoggerFactory.getLogger(RestFeature.class);

    private HttpServer httpServer;

    @Override
    public String unique() {
        return "rest";
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
        if(!AppContext.CONFIG.getBoolean(unique() + ".enable")) {
            this.stop(startPromise);
            return;
        }
        httpServer = getVertx().createHttpServer();
        logger.info("正在启动 Rest 模块……");
        Router router = Router.router(vertx);
        router.route().handler(ctx -> {
            HttpServerResponse response = ctx.response();
            response.setChunked(true);
            response.putHeader("content-type", "text/plain");
            response.end("Hello World from Vert.x-Web!");
        });
        int port = AppContext.CONFIG.getInt("server.port");
        httpServer.requestHandler(router).listen(port, r -> {
            if (r.succeeded()) {
                logger.info("Rest 模块启动完成：{}", port);
            } else {
                logger.error("Rest 模块启动出错", r.cause());
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
}
