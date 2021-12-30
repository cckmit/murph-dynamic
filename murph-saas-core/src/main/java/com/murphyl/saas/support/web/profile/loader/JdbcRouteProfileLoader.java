package com.murphyl.saas.support.web.profile.loader;

import com.murphyl.saas.support.web.profile.RestRoute;

import java.util.List;
import java.util.Map;

/**
 * JDBC - Rest 配置加载
 *
 * @date: 2021/12/24 15:37
 * @author: murph
 */
public class JdbcRouteProfileLoader extends AbstractRouteProfileLoader {

    public JdbcRouteProfileLoader(Map<String, Object> options) {
    }

    @Override
    public String name() {
        return "jdbc";
    }

    @Override
    public List<RestRoute> load() {
        return null;
    }
}
