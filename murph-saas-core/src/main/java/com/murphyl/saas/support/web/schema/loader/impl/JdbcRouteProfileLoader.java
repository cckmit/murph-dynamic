package com.murphyl.saas.support.web.schema.loader.impl;

import com.murphyl.saas.support.web.schema.RestRoute;
import com.murphyl.saas.support.web.schema.manager.RestProfileLoader;

import java.util.List;
import java.util.Map;

/**
 * JDBC - Rest 配置加载
 *
 * @date: 2021/12/24 15:37
 * @author: murph
 */
public class JdbcRouteProfileLoader implements RestProfileLoader {

    public JdbcRouteProfileLoader(Map<String, Object> options) {
    }

    @Override
    public List<RestRoute> load() {
        return null;
    }
}
