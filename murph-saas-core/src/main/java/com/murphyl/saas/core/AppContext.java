package com.murphyl.saas.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 应用 - 上下文
 *
 * @author: murph
 * @date: 2021/12/23 - 1:55
 */
public final class AppContext {

    private final static AppContext INSTANCE;

    public final static Config CONFIG;

    static {
        CONFIG = ConfigFactory.load();
        INSTANCE = new AppContext();
    }

    private AppContext() {
    }



}
