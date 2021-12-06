package com.murphyl.etl.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;

/**
 * -
 *
 * @date: 2021/12/7 1:07
 * @author: murph
 */
public class JdbcSupport {

    protected Connection getDatabaseConnection(String url) {
        try {
            String replaced = StringUtils.joinWith(":p6spy:", StringUtils.split(url, ":", 2));
            return DriverManager.getConnection(replaced);
        } catch (SQLException e) {
            throw new IllegalStateException("get jdbc connection error: " + url, ExceptionUtils.getRootCause(e));
        }
    }


}
