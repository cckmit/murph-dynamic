package com.murphyl.saas.modules.web.router.devops;

import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;
import org.graalvm.polyglot.Value;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 开放用户前端 - 动态路由 - 管理器
 *
 * @author: murph
 * @date: 2021/12/26 - 9:25
 */
@Singleton
public class DynamicFrontRouterManager {

    @Inject
    private RouteProfileLoader routeProfileLoader;

    public List<RestRoute<Value>> load() {
        return routeProfileLoader.load();
    }

}
