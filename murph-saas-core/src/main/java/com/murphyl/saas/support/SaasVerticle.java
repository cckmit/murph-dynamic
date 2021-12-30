package com.murphyl.saas.support;

import com.murphyl.saas.core.SaasFeature;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Vercle - 包装
 *
 * @author: murph
 * @date: 2021/12/26 - 1:45
 */
public class SaasVerticle implements Verticle {

    private Vertx vertx;

    protected Context context;

    private SaasFeature feature;

    public SaasVerticle(SaasFeature feature) {
        this.feature = feature;
    }

    @Override
    public Vertx getVertx() {
        return this.vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.context = context;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        feature.init(vertx);
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        feature.stop();
        stopPromise.complete();
    }

}
