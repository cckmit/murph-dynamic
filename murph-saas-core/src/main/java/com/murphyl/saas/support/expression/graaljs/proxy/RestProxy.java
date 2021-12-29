package com.murphyl.saas.support.expression.graaljs.proxy;

import io.vertx.ext.web.RoutingContext;
import org.graalvm.polyglot.HostAccess;

/**
 * -
 *
 * @date: 2021/12/23 21:42
 * @author: murph
 */
@HostAccess.Implementable
public class RestProxy {

    private final RoutingContext context;

    public RestProxy(RoutingContext context) {
        this.context = context;
    }

    @HostAccess.Export
    public String path() {
        return context.request().path();
    }

    @HostAccess.Export
    public void sendJSON(Object value) {
        context.json(value);
    }

}
