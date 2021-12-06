package com.murphyl.etl.core.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * -
 *
 * @date: 2021/12/6 12:51
 * @author: murph
 */
@Qualifier({"jdbc", "sql"})
public class JdbcExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Properties stepProps) {
        String jdbcUrl = stepProps.getProperty("connect");
        Connection jdbcConnection = getDatabaseConnection(jdbcUrl);
        ResultSet result = doQuery(jdbcConnection, dsl);
        return resultSet2Dataframe(result);
    }

    private Connection getDatabaseConnection(String url) {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new IllegalStateException("get jdbc connection error: " + url, ExceptionUtils.getRootCause(e));
        }
    }

    private ResultSet doQuery(Connection jdbcConnection, String dsl) {
        try {
            Statement statement = jdbcConnection.createStatement();
            return statement.executeQuery(dsl);
        } catch (SQLException e) {
            throw new IllegalStateException("execute sql error: " + dsl, ExceptionUtils.getRootCause(e));
        }
    }

    public Dataframe resultSet2Dataframe(ResultSet result) {
        try {
            ResultSetMetaData metaData = result.getMetaData();
            int columnsCount = metaData.getColumnCount();
            String[] columnLabels = new String[columnsCount];
            for (int i = 1; i < columnsCount + 1; i++) {
                columnLabels[i - 1] = metaData.getColumnLabel(i);
            }
            List<Object[]> rows = new ArrayList<>();
            Object[] row;
            while (result.next()) {
                row = new Object[columnLabels.length];
                rows.add(row);
                for (int columnIndex = 0; columnIndex < columnLabels.length; columnIndex++) {
                    row[columnIndex] = result.getObject(columnLabels[columnIndex]);
                }
            }
            return new Dataframe(columnLabels, rows);
        } catch (SQLException e) {
            throw new IllegalStateException("deal result set metadata error: ", ExceptionUtils.getRootCause(e));
        }
    }
}
