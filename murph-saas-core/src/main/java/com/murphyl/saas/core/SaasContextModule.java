package com.murphyl.saas.core;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.murphyl.saas.support.web.profile.RouteProfile;
import com.murphyl.saas.support.web.profile.loader.FilesystemRouteProfileLoader;
import com.murphyl.saas.support.web.profile.loader.JdbcRouteProfileLoader;
import com.murphyl.saas.support.web.profile.manager.RouteProfileLoader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IoC - 上下文怕配置
 *
 * @author: murph
 * @date: 2021/12/26 - 4:20
 */
public class SaasContextModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(SaasContextModule.class);

    @Override
    protected void configure() {
        // 配置
        Config configModule = ConfigFactory.load();
        bind(Config.class).toProvider(() -> configModule).in(Scopes.SINGLETON);
        // 路由规则加载器
        RouteProfileLoader routeProfileLoader = buildRestProfileLoader(configModule.getConfig("rest.profile"));
        bind(RouteProfileLoader.class).toProvider(() -> routeProfileLoader).in(Scopes.SINGLETON);

        // TODO datasource

    }

    private RouteProfileLoader buildRestProfileLoader(Config options) {
        logger.info("正在构造路由规则加载器：{}", options.withoutPath("options.routes"));
        RouteProfile profile = ConfigBeanFactory.create(options, RouteProfile.class);
        switch (profile.getLoader()) {
            case "filesystem":
                return new FilesystemRouteProfileLoader(profile.getOptions());
            case "jdbc":
                return new JdbcRouteProfileLoader(profile.getOptions());
            default:
                throw new IllegalStateException("无法构造指定类型的路由规则加载器：" + options);
        }
    }

}
