package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.etl.support.JdbcSupport;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * -
 *
 * @date: 2021/12/6 12:53
 * @author: murph
 */
public class JdbcLoader extends JdbcSupport implements Loader {

    @Override
    public void load(String dsl, Dataframe dataframe, Properties stepProps) {
        String jdbcUrl = stepProps.getProperty("connect");
        Connection jdbcConnection = getDatabaseConnection(jdbcUrl);
        executeSql(jdbcConnection, dsl);
    }

    private boolean executeSql(Connection jdbcConnection, String dsl) {
        try {
            return jdbcConnection.createStatement().execute(dsl);
        } catch (SQLException e) {
            throw new IllegalStateException("execute sql error: " + dsl, ExceptionUtils.getRootCause(e));
        }
    }

}
