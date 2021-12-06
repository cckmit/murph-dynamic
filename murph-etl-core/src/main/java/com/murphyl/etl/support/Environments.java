package com.murphyl.etl.support;

import com.google.common.collect.HashBasedTable;
import com.google.common.primitives.Ints;
import com.murphyl.dynamic.Feature;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 环境工具类
 *
 * @date: 2021/11/11 11:06
 * @author: murph
 */
public final class Environments {

    private static final Logger logger = LoggerFactory.getLogger(Environments.class);

    public static final Dotenv DOT_ENV;

    /**
     * 任务命令行参数 - 时间戳索引
     */
    public static final int CLI_TS_ARG_INDEX = 0;
    /**
     * 任务命令行参数 - 任务Schema索引
     */
    public static final int JOB_SCHEMA_ARG_INDEX = 1;

    private static final JsonSchemaFactory JSON_SCHEMA_FACTORY;

    private static final HashBasedTable<Class, String, Feature> DYNAMIC_FEATURE = HashBasedTable.create();

    static {
        // 读取本地配置
        String workdir = System.getProperty("murph-etl.workdir", SystemUtils.USER_DIR);
        logger.info("read variables from .env file in: {}", workdir);
        try {
            DOT_ENV = Dotenv.configure().directory(workdir).load();
            logger.info("runtime environments: {}", DOT_ENV.entries());
        } catch (Exception e) {
            throw new IllegalStateException("read .env file error", ExceptionUtils.getRootCause(e));
        }
        // 加载插件
        JSON_SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        Iterator<Feature> featureIterator = ServiceLoader.load(Feature.class).iterator();
        while (featureIterator.hasNext()) {
            Feature feature = featureIterator.next();
            Class[] interfaces = feature.getClass().getInterfaces();
            for (Class group : interfaces) {
                logger.info("dynamic feature ({}) group by ({}) with alias {}", feature, group.getCanonicalName(), feature.alias());
                for (String alias : feature.alias()) {
                    DYNAMIC_FEATURE.put(group, alias, feature);
                }
            }
        }
    }

    public static <T extends Feature> T getFeature(Class<T> group, String unique) {
        return (T) DYNAMIC_FEATURE.get(group, unique);
    }

    /**
     * 用于测试支持
     *
     * @param path
     * @return
     */
    public static String getResource(String path) {
        return Paths.get(SystemUtils.USER_DIR, path).toAbsolutePath().toString();
    }

    public static InputStream getResourceAsStream(String uri) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);
    }

    /**
     * 读取配置
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return DOT_ENV.get(key);
    }

    /**
     * 读取配置
     *
     * @param key
     * @param value
     * @return
     */
    public static String get(String key, String value) {
        return DOT_ENV.get(key, value);
    }

    public static int getInt(String key, int defaultValue) {
        return NumberUtils.toInt(DOT_ENV.get(key), defaultValue);
    }

    public static JsonSchema getJsonSchema(String schemaUri) {
        return JSON_SCHEMA_FACTORY.getSchema(getResourceAsStream(schemaUri));
    }

}
