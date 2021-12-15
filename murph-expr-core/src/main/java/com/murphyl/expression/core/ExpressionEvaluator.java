package com.murphyl.expression.core;

import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * 表达式 - 执行器
 *
 * @date: 2021/12/2 20:14
 * @author: murph
 */
@Group
public interface ExpressionEvaluator extends Feature {

    Pattern EXPR_PATTERN = Pattern.compile("(^\\$\\{)(.+)(\\}$)");

    /**
     * 合法的表达式
     *
     * @param expression
     * @return
     */
    default boolean valid(String expression) {
        String expr = null == expression ? null : expression.trim();
        return null != expr && expr.length() > 0 && EXPR_PATTERN.matcher(expr).find();
    }

    /**
     * 获取干净的表达式
     *
     * @param expression
     * @return
     */
    default String clean(String expression) {
        return valid(expression) ? expression.substring(2, expression.length() - 1) : null;
    }


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
     * @param clean
     * @return
     */
    Object eval(String expr, Map<String, Object> params, boolean clean);

}
