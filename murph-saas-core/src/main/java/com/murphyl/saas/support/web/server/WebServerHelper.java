package com.murphyl.saas.support.web.server;

import com.murphyl.saas.support.expression.graaljs.proxy.LoggerProxy;
import com.murphyl.saas.support.expression.graaljs.proxy.RestProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/26 - 9:34
 */
public final class WebServerHelper {

    private static final Logger logger = LoggerFactory.getLogger(WebServerHelper.class);

    private static final String SERVER_HOST_KEY = "server.host";

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
