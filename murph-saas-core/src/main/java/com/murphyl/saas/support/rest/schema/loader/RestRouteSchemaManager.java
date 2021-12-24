package com.murphyl.saas.support.rest.schema.loader;

import com.murphyl.saas.support.Environments;
import com.murphyl.saas.support.rest.schema.RestProfile;
import com.murphyl.saas.support.rest.schema.RestRoute;
import com.murphyl.saas.support.rest.schema.loader.impl.FilesystemRestRouteSchemaManager;
import com.murphyl.saas.support.rest.schema.loader.impl.JdbcRestProfileLoader;
import com.typesafe.config.ConfigBeanFactory;

import java.util.List;

/**
 * Rest - 配置加载器
 *
 * @date: 2021/12/24 15:35
 * @author: murph
 */
public interface RestRouteSchemaManager {

    /**
     * 构造实例
     *
     * @return
     */
    static RestRouteSchemaManager getInstance() {
        RestProfile profile = ConfigBeanFactory.create(Environments.getConfig("rest.profile"), RestProfile.class);
        if (null == profile || null == profile.getLoader()) {
            throw new IllegalStateException("加载 Rest 模块的配置出错");
        }
        switch (profile.getLoader()) {
            case "filesystem":
                return new FilesystemRestRouteSchemaManager(profile.getOptions());
            case "jdbc":
                return new JdbcRestProfileLoader(profile.getOptions());
            default:
                throw new IllegalStateException("Rest 模块配置加载器初始化失败，暂不支持的加载器类型：" + profile.getLoader());
        }
    }

    /**
     * 生成 namespace
     *
     * @param type
     * @param unique
     * @param endpoint
     * @return
     */
    default String namespace(String type, String unique, String endpoint) {
        return String.format("%s://%s?%s", type, unique, endpoint);
    }

    /**
     * 加载配置
     *
     * @return
     * @throws Exception
     */
    List<RestRoute> loadRoutes();

}
