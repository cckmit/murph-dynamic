package com.murphyl.saas.support.web.schema;

import com.murphyl.saas.support.web.schema.loader.impl.FilesystemRouteProfileLoader;
import com.murphyl.saas.support.web.schema.loader.impl.JdbcRouteProfileLoader;
import com.murphyl.saas.support.web.schema.manager.RestProfileLoader;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/26 - 7:37
 */
@Singleton
public class RouteProfileLoaderBuilder {

    private RouteProfile profile;

    @Inject
    public RouteProfileLoaderBuilder(Config env) throws ClassNotFoundException {
        this.profile = ConfigBeanFactory.create(env.getConfig("rest.profile"), RouteProfile.class);
    }

    public RestProfileLoader build() {
        switch (profile.getLoader()) {
            case "filesystem":
                return new FilesystemRouteProfileLoader(profile.getOptions());
            case "jdbc":
                return new JdbcRouteProfileLoader(profile.getOptions());
        }
        return null;
    }

}
