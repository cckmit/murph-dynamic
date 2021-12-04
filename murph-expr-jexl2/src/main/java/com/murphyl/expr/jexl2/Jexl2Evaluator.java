package com.murphyl.expr.jexl2;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.expr.core.ExpressionEvaluator;
import com.murphyl.expr.core.UserDefinedFunction;
import com.murphyl.expr.support.PreparedExpressions;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

/**
 * JEXL - 表达式执行器
 *
 * @date: 2021/12/2 20:15
 * @author: murph
 */
@Qualifier({"jexl2", "jexl"})
public class Jexl2Evaluator implements ExpressionEvaluator {

    private static final Pattern EXPR_PATTERN = Pattern.compile("(^\\$\\{)(.+)(\\}$)");

    private final JexlEngine engine;

    public Jexl2Evaluator() {
        this.engine = new JexlEngine();
    }

    @Override
    public void setUdf(Map<String, Class> udfMapper) {
        Map<String, Object> functions = new HashMap<>();
        functions.put(null, PreparedExpressions.class);
        Iterator<UserDefinedFunction> udfIterator = ServiceLoader.load(UserDefinedFunction.class).iterator();
        UserDefinedFunction udf;
        while (udfIterator.hasNext()) {
            udf = udfIterator.next();
            if (null == udf.namespace()) {
                continue;
            }
            functions.put(udf.namespace(), udf);
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
