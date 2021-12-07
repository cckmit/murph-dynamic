package com.murphyl.etl.support.expression;

import com.murphyl.datasource.DataSourceBuilder;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.expr.core.MurphExprUdf;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * 数据库连接池 - Expression UDF
 *
 * @date: 2021/12/7 9:33
 * @author: murph
 */
@Qualifier("datasource")
public class DataSourceFunctions implements MurphExprUdf {

    public DataSource connect(String url) {
        return connect(url, null);
    }

    public DataSource connect(String url, Map<String, Object> config) {
        DataSourceBuilder builder = new DataSourceBuilder();
        builder.setJdbcUrl(url);
        if (null != config) {
            Properties properties = new Properties();
            properties.putAll(config);
            builder.setProperties(properties);
        }
        return builder.build();
    }

}
