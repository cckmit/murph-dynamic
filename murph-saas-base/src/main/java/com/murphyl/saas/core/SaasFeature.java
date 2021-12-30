package com.murphyl.saas.core;

import com.murphyl.dynamic.NamedFeature;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/22 - 23:56
 */
public interface SaasFeature<T> {

    /**
     * 依赖项
     *
     * @param instance
     */
    void init(T instance);

    /**
     * 结束方法
     *
     * @throws Exception
     */
    default void stop() {

    }


}
