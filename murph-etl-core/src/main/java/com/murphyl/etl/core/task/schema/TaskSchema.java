package com.murphyl.etl.core.task.schema;

import java.util.Map;

/**
 * -
 *
 * @date: 2021/12/17 15:14
 * @author: murph
 */
public interface TaskSchema {

    /**
     * 获取 DSL
     *
     * @return
     */
    String dsl();

    /**
     * 获取属性
     *
     * @param key
     * @return
     */
    String attr(String key);

    /**
     * 获取节点所有属性
     *
     * @return
     */
    Map<String, String> attrs();

}
