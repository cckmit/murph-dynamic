package com.murphyl.expr.jexl;

import com.murphyl.expr.core.ExpressionEvaluator;
import com.murphyl.expr.support.PreparedExpressions;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JEXL - 表达式执行器
 *
 * @date: 2021/12/2 20:15
 * @author: murph
 */
public class JexlEvaluator implements ExpressionEvaluator {

    private static final Pattern EXPR_PATTERN = Pattern.compile("(^\\$\\{)(.+)(\\}$)");

    private final JexlEngine engine;

    public JexlEvaluator() {
        this.engine = new JexlEngine();
    }

    @Override
    public String[] getAlias() {
        return new String[]{"jexl"};
    }

    @Override
    public void setUdf(Map<String, Class> udfMapper) {
        Map<String, Object> functions = new HashMap<>();
        functions.put(null, PreparedExpressions.class);
        if (null != udfMapper) {
            if (udfMapper.containsKey(null)) {
                udfMapper.remove(null);
            }
            if (!udfMapper.isEmpty()) {
                functions.putAll(udfMapper);
            }
        }
        this.engine.setFunctions(functions);
    }

    @Override
    public Object eval(String expr) {
        return eval(expr, null);
    }

    @Override
    public Object eval(String expr, Map<String, Object> params) {
        if (null == expr || !EXPR_PATTERN.matcher(expr).find()) {
            return expr;
        }
        String temp = expr.substring(2, expr.length() - 1);
        MapContext context = new MapContext();
        if (null != params) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
        return engine.createExpression(temp).evaluate(context);
    }
}
