package com.murphyl.datasource;

import com.murphyl.dynamic.Feature;
import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 动态数据源 - 插件
 *
 * @author: murph
 * 2021/12/3 - 21:51
 */
public final class DataSourceBuilder implements Feature {

    private HikariConfig config;

    private boolean useP6Spy = true;

    public DataSourceBuilder() {
        this.config = new HikariConfig();
    }

    public DataSourceBuilder setJdbcUrl(String url) {
        this.config.setJdbcUrl(url);
        return this;
    }

    public DataSourceBuilder setProperties(Properties properties) {
        if (null != properties) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                this.config.addDataSourceProperty(entry.getKey().toString(), entry.getValue());
            }
        }
        return this;
    }

    public DataSourceBuilder useP6Spy(Boolean useP6Spy) {
        this.useP6Spy = useP6Spy;
        return this;
    }

    public DataSource build() {
        Objects.requireNonNull(config.getJdbcUrl(), "jdbc url can not be null");
        DataSource ds = new HikariDataSource(config);
        return useP6Spy ? new P6DataSource(ds) : ds;
    }

}
