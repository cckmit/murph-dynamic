package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.utils.TaskStepUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringJoiner;

/**
 * JDBC - Data Loader
 *
 * @date: 2021/12/6 12:53
 * @author: murph
 */
@Qualifier({"jdbc", "sql"})
public class JdbcLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(JdbcLoader.class);


    @Override
    public void load(String dsl, Dataframe dataframe, Map<String, Object> stepProps) {
        try {
            Connection jdbcConnection = TaskStepUtils.getJdbcConnection(stepProps);
            if (null != dataframe && ArrayUtils.isNotEmpty(dataframe.getValues())) {
                StringJoiner joiner = new StringJoiner(";");
                for (int i = 0; i < dataframe.getValues().length; i++) {
                    joiner.add(StringSubstitutor.replace(dsl, dataframe.row(i), "#{", "}"));
                }
                int[] result = executeUpdate(jdbcConnection, joiner.toString());
                logger.info("jdbc loader execute complete: {}", result.length);
            } else {
                int[] result = executeUpdate(jdbcConnection, dsl);
                logger.info("jdbc loader execute complete: {}", result.length);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("load data error", ExceptionUtils.getRootCause(e));
        }
    }

    private int[] executeUpdate(Connection jdbcConnection, String dsl) throws SQLException {
        Statement statement = jdbcConnection.createStatement();
        String[] items = StringUtils.split(dsl, ';');
        for (String sql : items) {
            if(StringUtils.isBlank(sql)) {
                continue;
            }
            statement.addBatch(sql);
        }
        return statement.executeBatch();
    }

}
