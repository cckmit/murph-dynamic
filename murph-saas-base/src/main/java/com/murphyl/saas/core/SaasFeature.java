package com.murphyl.saas.core;

import io.vertx.core.Verticle;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/22 - 23:56
 */
public interface SaasFeature extends Verticle {

    /**
     * 设置别名
     *
     * @return
     */
    String unique();

}
