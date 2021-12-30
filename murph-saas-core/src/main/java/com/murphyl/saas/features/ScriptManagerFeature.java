package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.typesafe.config.Config;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * 表达式、模板 - 特征
 *
 * @author: murph
 * @date: 2021/12/26 - 0:37
 */
public class ScriptManagerFeature implements SaasFeature<Vertx> {

    private static final Logger logger = LoggerFactory.getLogger(ScriptManagerFeature.class);

    @Inject
    private Config env;

    @Override
    public String name() {
        return "expression";
    }

    @Override
    public void init(Vertx instance) {
        logger.info("表达式管理器：{}", env.getObject(name()));

    }

}
