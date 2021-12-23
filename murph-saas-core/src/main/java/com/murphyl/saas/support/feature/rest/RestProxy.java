package com.murphyl.saas.support.feature.rest;

import io.vertx.ext.web.RoutingContext;
import org.graalvm.polyglot.HostAccess;

/**
 * -
 *
 * @date: 2021/12/23 21:42
 * @author: murph
 */
public class RestProxy {

    private final RoutingContext context;

    public RestProxy(RoutingContext context) {
        this.context = context;
    }

    @HostAccess.Export
    public RestProxy json(Object value) {
        context.json(value);
        return this;
    }

    public RestProxy end() {
        context.end();
        return this;
    }

}
