package com.murphyl.expresssion.core;

import com.murphyl.dynamic.Feature;

import java.util.Map;

/**
 * 表达式 - 执行引擎
 *
 * @date: 2021/12/23 9:51
 * @author: murph
 */
public interface ExpressionEngine extends Feature {

    /**
     * 执行表达式
     *
     * @param expression
     * @param params
     * @return
     */
    Object eval(String expression, Map<String, Object> params);

    /**
     * 执行表达式
     *
     * @param expression
     * @return
     */
    Object eval(String expression);

}
