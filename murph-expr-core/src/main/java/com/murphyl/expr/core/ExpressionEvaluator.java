package com.murphyl.expr.core;

import java.util.Map;

/**
 * 表达式 - 执行器
 *
 * @date: 2021/12/2 20:14
 * @author: murph
 */
public interface ExpressionEvaluator {

    /**
     * 注册的名称
     *
     * @return
     */
    String[] getAlias();

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

    /**
     * 配置 UDF
     *
     * @param udfMapper
     */
    void setUdf(Map<String, Class> udfMapper);

}
