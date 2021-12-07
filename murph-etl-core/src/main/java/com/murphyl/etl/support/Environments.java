package com.murphyl.etl.support;

import com.google.common.collect.HashBasedTable;
import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.stream.Stream;

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


    private static final HashBasedTable<Class, String, Feature> DYNAMIC_FEATURE = HashBasedTable.create();

    static {
        // 读取本地配置
        String workdir = System.getProperty("murph-etl.workdir", SystemUtils.USER_DIR);
        logger.info("read variables from .env file in: {}", workdir);
        try {
            DOT_ENV = Dotenv.configure().ignoreIfMalformed().directory(workdir).load();
            logger.info("runtime environments: {}", DOT_ENV.entries());
        } catch (Exception e) {
            throw new IllegalStateException("read .env file error", ExceptionUtils.getRootCause(e));
        }
        // 加载动态特性
        registerDynamicFeatures();
    }

    /**
     * 注册插件
     */
    private static void registerDynamicFeatures() {
        Iterator<Feature> featureIterator = ServiceLoader.load(Feature.class).iterator();
        Feature feature;
        Class[] interfaces;
        while (featureIterator.hasNext()) {
            feature = featureIterator.next();
            interfaces = feature.getClass().getInterfaces();
            for (Class group : interfaces) {
                if (!group.isAnnotationPresent(Group.class) || ArrayUtils.isEmpty(feature.alias())) {
                    continue;
                }
                logger.info("dynamic feature ({}) with group ({}) and alias {}", feature, group.getCanonicalName(), feature.alias());
                for (String alias : feature.alias()) {
                    if (StringUtils.isEmpty(alias)) {
                        continue;
                    }
                    DYNAMIC_FEATURE.put(group, StringUtils.trim(alias), feature);
                }
            }
        }
    }

    public static <T extends Feature> T getFeature(Class<T> group, String unique) {
        if (StringUtils.isEmpty(unique)) {
            return null;
        }
        return (T) DYNAMIC_FEATURE.get(group, StringUtils.trim(unique));
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

    public static Stream<URL> resources(String uri) {
        return Thread.currentThread().getContextClassLoader().resources(uri);
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

}
