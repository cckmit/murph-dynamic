package com.murphyl.saas.support.web.profile.manager;

import com.murphyl.dynamic.NamedFeature;

import java.util.List;

/**
 * Rest - 配置加载器
 *
 * @date: 2021/12/24 15:35
 * @author: murph
 */
public interface RouteProfileLoader extends NamedFeature {

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
