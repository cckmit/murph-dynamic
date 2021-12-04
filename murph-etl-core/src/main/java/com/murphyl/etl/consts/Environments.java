package com.murphyl.etl.consts;

import com.google.common.collect.HashBasedTable;
import com.murphyl.dynamic.Feature;
import com.murphyl.etl.task.loader.Loader;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 环境工具类
 *
 * @date: 2021/11/11 11:06
 * @author: murph
 */
public final class Environments {

    private static final Logger logger = LoggerFactory.getLogger(Environments.class);

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
        return Paths.get(System.getProperty("user.dir"), path).toAbsolutePath().toString();
    }

    public static InputStream getResourceAsStream(String uri) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);
    }


    public static String get(String key) {
        if (System.getProperties().containsKey(key)) {
            return System.getProperty(key);
        }
        if (System.getenv().containsKey(key)) {
            return System.getenv(key);
        }
        return null;
    }

    public static synchronized <T extends Feature> Map<String, T> loadService(String name, Class<T> tClass) {
        Iterator<T> factories = loadServices(tClass);
        Map<String, T> result = new ConcurrentHashMap<>();
        T service;
        while (factories.hasNext()) {
            service = factories.next();
            String[] alias = service.alias();
            if (ArrayUtils.isEmpty(alias)) {
                continue;
            }
            for (String item : alias) {
                result.put(item, service);
                logger.info("{} ({}) alias [{}] registered", name, service, item);
            }
        }
        return result;
    }

    public static <T> Iterator<T> loadServices(Class<T> tClass) {
        return ServiceLoader.load(tClass).iterator();
    }

    public static JsonSchema getJsonSchema(String schemaUri) {
        return JSON_SCHEMA_FACTORY.getSchema(getResourceAsStream(schemaUri));
    }


}
