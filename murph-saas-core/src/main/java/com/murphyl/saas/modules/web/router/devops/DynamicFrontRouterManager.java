package com.murphyl.saas.modules.web.router.devops;

import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;
import org.graalvm.polyglot.Value;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * 开放用户前端 - 动态路由 - 管理器
 *
 * @author: murph
 * @date: 2021/12/26 - 9:25
 */
@Singleton
public class DynamicFrontRouterManager {

    private RouteProfileLoader routeProfileLoader;

    private static final List<RestRoute<Value>> LOADED_ROUTES = new ArrayList<>();

    private static final LongAdder DYNAMIC_ROUTES_VERSION = new LongAdder();

    @Inject
    public DynamicFrontRouterManager(RouteProfileLoader routeProfileLoader) {
        this.routeProfileLoader = routeProfileLoader;
        this.reload();
    }

    public boolean reload() {
        try {
            LOADED_ROUTES.clear();
            return LOADED_ROUTES.addAll(routeProfileLoader.load());
        } finally {
            DYNAMIC_ROUTES_VERSION.increment();
        }
    }

    public List<RestRoute<Value>> getLoadedRoutes() {
        return LOADED_ROUTES;
    }

    public long getRoutesVersion() {
        return DYNAMIC_ROUTES_VERSION.longValue();
    }

}
