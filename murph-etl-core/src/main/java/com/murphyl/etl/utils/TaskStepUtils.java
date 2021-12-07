package com.murphyl.etl.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.murphyl.datasource.DataSourceBuilder;
import com.murphyl.etl.support.Environments;
import com.murphyl.expr.core.ExpressionEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 子任务 - 批次支持
 *
 * @date: 2021/12/6 18:42
 * @author: murph
 */
public final class TaskStepUtils {

    private final static Cache<String, Object> STEP_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .recordStats()
            .build();

    /**
     * 获取批次信息
     *
     * @param params
     * @return
     */
    public static Integer getBatchSize(Map<String, Object> params) {
        String value = get(params, "batchSize", String.class);
        return null == value ? Environments.getInt("BATCH_KEY", 1000) : NumberUtils.toInt(value);
    }

    public static <T> T get(Map<String, Object> params, String key, Class<T> tClass) {
        Object value = params.get(key);
        return null == value ? null : tClass.cast(value);
    }

    public static Connection getJdbcConnection(Map<String, Object> properties) throws SQLException {
        DataSource dataSource = get(properties, "datasource", DataSource.class);
        if (null != dataSource) {
            return dataSource.getConnection();
        }
        String url = get(properties, "connect", String.class);
        if (StringUtils.isEmpty(url)) {
            throw new IllegalStateException("step properties [ datasource, connect ] can not be null at same time");
        }
        try {
            dataSource = (DataSource) STEP_CACHE.get(url, () -> new DataSourceBuilder().jdbcUrl(url).build());
        } catch (ExecutionException e) {
            throw new IllegalStateException("can not create datasource: " + url);
        }
        if (null != dataSource) {
            return dataSource.getConnection();
        }
        throw new IllegalStateException("can not get jdbc connection from: " + url);
    }

    /**
     * 获取表达式执行引擎
     *
     * @param engineName
     * @return
     */
    public static ExpressionEvaluator getExprEvaluator(String engineName) {
        if (StringUtils.isBlank(engineName)) {
            return getDefaultExprEvaluator();
        }
        ExpressionEvaluator engine = Environments.getFeature(ExpressionEvaluator.class, engineName);
        Objects.requireNonNull(engine, "no matched expression evaluate engine: " + engineName);
        return engine;
    }

    /**
     * 获取默认表达式执行引擎
     *
     * @return
     */
    public static ExpressionEvaluator getDefaultExprEvaluator() {
        String name = Environments.get("EXPRESSION_ENGINE");
        Validate.notBlank(name, "please set expression evaluate engine in .env file use key: EXPRESSION_ENGINE");
        ExpressionEvaluator engine = Environments.getFeature(ExpressionEvaluator.class, name);
        Objects.requireNonNull(engine, "no default expression evaluate engine: " + name);
        return engine;
    }

}
