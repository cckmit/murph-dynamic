package com.murphyl.expr.core;

import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;

import java.util.Map;

/**
 * 表达式 - 执行器
 *
 * @date: 2021/12/2 20:14
 * @author: murph
 */
@Group
public interface ExpressionEvaluator extends Feature {

    /**
     * 执行表达式实现
     *
     * @param expr
     * @return
     */
    Object eval(String expr);

    /**
     * 执行表达式实现
     *
     * @param expr
     * @param params
     * @return
     */
    Object eval(String expr, Map<String, Object> params);

}
