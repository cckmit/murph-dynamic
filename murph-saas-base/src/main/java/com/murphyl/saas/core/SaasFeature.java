package com.murphyl.saas.core;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/22 - 23:56
 */
public interface SaasFeature<T>  {

    /**
     * 设置别名
     *
     * @return
     */
    String unique();

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
