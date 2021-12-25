package com.murphyl.saas.features;

import com.murphyl.saas.core.SaasFeature;
import com.typesafe.config.Config;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * 数据源 - 构造门面
 *
 * @author: murph
 * @date: 2021/12/26 - 0:33
 */
public class DataSourceFeature implements SaasFeature<Vertx> {

    private static final Logger logger = LoggerFactory.getLogger(WebFacadeFeature.class);

    @Inject
    public DataSourceFeature() {
    }

    @Override
    public String name() {
        return "datasource";
    }

    @Override
    public void init(Vertx instance) {

    }
}
