package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.typesafe.config.Config;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * 管线 - 管理器模块
 *
 * @date: 2021/12/30 11:12
 * @author: murph
 */
public class PipelineManagerFeature implements SaasFeature<Vertx> {

    private static final Logger logger = LoggerFactory.getLogger(PipelineManagerFeature.class);

    @Inject
    private Config env;

    @Override
    public void init(Vertx instance) {
        logger.info("Starting pipeline manager……");
    }

}
