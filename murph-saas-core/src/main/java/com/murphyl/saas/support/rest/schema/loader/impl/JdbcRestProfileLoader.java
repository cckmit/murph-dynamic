package com.murphyl.saas.support.rest.schema.loader.impl;

import com.murphyl.saas.support.rest.schema.RestRoute;
import com.murphyl.saas.support.rest.schema.loader.RestRouteSchemaManager;

import java.util.List;
import java.util.Map;

/**
 * JDBC - Rest 配置加载
 *
 * @date: 2021/12/24 15:37
 * @author: hao.luo <hao.luo@china.zhaogang.com>
 */
public class JdbcRestProfileLoader implements RestRouteSchemaManager {

    public JdbcRestProfileLoader(Map<String, Object> properties) {
    }

    @Override
    public List<RestRoute> load() {
        return null;
    }
}
