package com.murphyl.saas.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.murphyl.saas.support.web.schema.manager.RestRouteSchemaManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * IoC - 上下文怕配置
 *
 * @author: murph
 * @date: 2021/12/26 - 4:20
 */
public class SaasContextModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(SaasContextModule.class);

    private static final String DATASOURCE_CONFIG_KEY = "datasource";

    @Override
    protected void configure() {
        Config config = ConfigFactory.load();
        // 绑定上下文配置
        this.bind(Config.class).toProvider(() -> config).in(Singleton.class);
        // 初始化数据源
        if (config.hasPath(DATASOURCE_CONFIG_KEY)) {
            provideDataSources(config.getObject(DATASOURCE_CONFIG_KEY));
        }
    }

    /**
     * 初始化数据源模块
     *
     * @param dataSources
     */
    public void provideDataSources(ConfigObject dataSources) {
        for (Map.Entry<String, ConfigValue> entry : dataSources.entrySet()) {
            logger.info("TODO - 正在初始化数据源：{}", entry.getKey());
        }
    }

    @Provides
    @Singleton
    public Function<String, RestRouteSchemaManager> restRouteSchemaManagerBuilder(Config config) {
        ConfigObject managers = config.getObject("app.route.schema.manager");
        return (name) -> {
            ConfigValue managerName = managers.get(name);
            Objects.requireNonNull(managerName, "指定的 Web 服务路由配置管理器（" + name + "）不存在");
            try {
                Class managerClass = Class.forName(managerName.unwrapped().toString());
                Config options = config.getConfig("rest.profile.options");
                return (RestRouteSchemaManager) managerClass.getConstructor(Config.class).newInstance(options);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };
    }

}
