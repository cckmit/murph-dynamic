package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.utils.ExpressionUtils;
import com.murphyl.etl.utils.TaskStepUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

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
        Integer batchSize = TaskStepUtils.getBatchSize(stepProps);
        try {
            Connection jdbcConnection = TaskStepUtils.getJdbcConnection(stepProps);
            Statement statement = jdbcConnection.createStatement();
            if (null != dataframe && ArrayUtils.isNotEmpty(dataframe.getValues())) {
                int size = 0;
                for (Map<String, Object> row : dataframe.render()) {
                    Map<String, Object> params = new HashMap(row);
                    params.putAll(stepProps);
                    statement.addBatch(ExpressionUtils.eval(dsl, params).toString());
                    size ++;
                    if(size % batchSize == 0) {
                        int[] result = statement.executeBatch();
                        logger.info("jdbc loader execute batch: {}", result.length);
                        statement.clearBatch();
                        size = 0;
                    }
                }
                if(size > 0) {
                    int[] result = statement.executeBatch();
                    logger.info("jdbc loader execute batch: {}", result.length);
                }
            } else {
                boolean result = statement.execute(ExpressionUtils.eval(dsl, stepProps).toString());
                logger.info("jdbc loader execute complete: {}", result);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("load data error", ExceptionUtils.getRootCause(e));
        }
    }

}
