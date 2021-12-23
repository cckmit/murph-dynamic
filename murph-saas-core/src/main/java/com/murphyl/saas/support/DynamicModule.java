package com.murphyl.saas.support;

import com.google.common.base.Joiner;

/**
 * -
 *
 * @date: 2021/12/23 12:06
 * @author: murph
 */
public enum DynamicModule {

    REST_DAPI,
    REST_DASH,
    REST_METRICS,

    ;

    /**
     * 配置名称分隔符
     */
    private static final char SETTING_KEY_DELIMITER = '.';
    /**
     * 模块级别分隔符
     */
    private static final char MODULE_LEVEL_DELIMITER = '_';

    private final String root;

    private static final Joiner SETTING_PATH_JOINER = Joiner.on(SETTING_KEY_DELIMITER);

    DynamicModule() {
        root = name().replace(MODULE_LEVEL_DELIMITER, SETTING_KEY_DELIMITER).toLowerCase();
    }

    public boolean enabled() {
        return Environments.getBoolean(SETTING_PATH_JOINER.join(root, "enable"));
    }

}
