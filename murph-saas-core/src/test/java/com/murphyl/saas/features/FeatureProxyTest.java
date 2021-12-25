package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/26 - 1:34
 */
public class FeatureProxyTest implements Verticle {

    protected Vertx vertx;
    protected Context context;

    @Test
    public void test() {
        Class[] interfaces = {SaasFeature.class, Verticle.class};


    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.context = context;
    }

    @Override
    public void start(Promise<Void> promise) throws Exception {
        System.out.println("start");
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception {
        System.out.println("stop");
    }
}
