package com.murphyl.saas.support.web.schema.manager;

import com.murphyl.dynamic.NamedFeature;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Rest - 配置加载器
 *
 * @date: 2021/12/24 15:35
 * @author: murph
 */
public interface RestRouteSchemaManager extends NamedFeature {

    /**
     * 构造实例
     *
     * @return
     */
    static RestRouteSchemaManager getInstance(String name) {
        if (null == name || name.trim().length() == 0) {
            throw new IllegalStateException("加载 Rest 模块的配置出错");
        }
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Rest 模块配置加载器初始化失败，暂不支持的加载器类型：" + name);
    }

    @Override
    default String name() {
        return "rest";
    }

    /**
     * 生成 namespace
     *
     * @param type
     * @param unique
     * @param endpoint
     * @return
     */
    default String namespace(String type, String unique, String endpoint) {
        return String.format("%s://%s?%s", type, unique, endpoint);
    }

    /**
     * 加载配置
     *
     * @return
     * @throws Exception
     */
    <T> List<T> load();

}
