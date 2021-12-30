package com.murphyl.saas.core;

import com.google.inject.AbstractModule;
import com.murphyl.saas.modules.ResourceManager;
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
 * IoC - 上下文怕配置 - https://www.baeldung.com/guice
 *
 * @author: murph
 * @date: 2021/12/26 - 4:20
 */
public class SaasContextModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(SaasContextModule.class);

    @Override
    protected void configure() {
        Config configModule = ConfigFactory.load();
        // 配置
        bind(Config.class).toProvider(() -> configModule).asEagerSingleton();
        // 资源管理器
        String resourceLoader = configModule.getString(ResourceManager.RESOURCE_LOADER_KEY);
        bind(RouteProfileLoader.class).to(ResourceManager.Loaders.valueOf(resourceLoader).getLoaderClass());

        // TODO datasource
    }

    /**
     * 路由规则加载器
     *
     * @param configModule
     * @return
     */
    /**
    @Provides
    @Singleton
    @Inject
    public RouteProfileLoader restProfileLoader(Config configModule) {
        Config options = configModule.getConfig("rest.profile");
        logger.info("Creating front user route profile via configuration: {}", options.withoutPath("options.routes"));
        RouteProfile profile = ConfigBeanFactory.create(options, RouteProfile.class);
        switch (profile.getLoader()) {
            case "filesystem":
                return new FilesystemRouteProfileLoader(profile.getOptions());
            case "jdbc":
                return new JdbcRouteProfileLoader(profile.getOptions());
            default:
                // 无法构造指定类型的路由规则加载器
                throw new IllegalStateException("Can not create front user route profile via:" + options);
        }
    }
*   */
}
