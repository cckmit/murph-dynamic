package com.murphyl.etl.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * -
 *
 * @date: 2021/12/7 0:17
 * @author: murph
 */
public final class JdbcUtils {

    public static String getDatabaseType(String url) {
        return URI.create(StringUtils.removeStart(url, "jdbc:")).getScheme();
    }

}
