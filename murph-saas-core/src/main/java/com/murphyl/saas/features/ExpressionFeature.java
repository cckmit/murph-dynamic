package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import io.vertx.core.Vertx;

/**
 * 表达式、模板 - 特征
 *
 * @author: murph
 * @date: 2021/12/26 - 0:37
 */
public class ExpressionFeature  implements SaasFeature<Vertx> {

    @Override
    public String name() {
        return "expression";
    }

    @Override
    public void init(Vertx instance) {

    }

}
