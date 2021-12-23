package com.murphyl.saas.support;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * 应用 - 上下文
 *
 * @author: murph
 * @date: 2021/12/23 - 1:55
 */
public final class Environments {

    private final static Config CONFIG;

    static {
        CONFIG = ConfigFactory.load();
    }

    public static boolean getBoolean(String key) {
        return CONFIG.getBoolean(key);
    }

    public static int getInt(String key) {
        return CONFIG.getInt(key);
    }

    public static String getString(String key) {
        return CONFIG.getString(key);
    }

    public static Config getConfig(String key) {
        return CONFIG.getConfig(key);
    }

    public static boolean exists(String path) {
        return CONFIG.hasPath(path);
    }
}
