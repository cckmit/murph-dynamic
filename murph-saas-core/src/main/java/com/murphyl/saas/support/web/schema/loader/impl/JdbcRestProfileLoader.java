package com.murphyl.saas.support.web.schema.loader.impl;

import com.murphyl.saas.support.web.schema.RestRoute;
import com.murphyl.saas.support.web.schema.manager.RestRouteSchemaManager;
import com.typesafe.config.Config;

import java.util.List;

/**
 * JDBC - Rest 配置加载
 *
 * @date: 2021/12/24 15:37
 * @author: hao.luo <hao.luo@china.zhaogang.com>
 */
public class JdbcRestProfileLoader implements RestRouteSchemaManager {

    public JdbcRestProfileLoader(Config options) {
    }

    @Override
    public List<RestRoute> load() {
        return null;
    }
}
