package com.murphyl.expr.jexl2;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.expr.core.ExpressionEvaluator;
import com.murphyl.expr.core.UserDefinedFunction;
import com.murphyl.expr.support.PreparedExpressions;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

/**
 * JEXL - 表达式执行器
 * - https://commons.apache.org/proper/commons-jexl/
 * - http://commons.apache.org/proper/commons-jexl/reference/syntax.html
 * - http://commons.apache.org/proper/commons-jexl/reference/examples.html
 * - https://www.tabnine.com/code/java/methods/org.apache.commons.jexl2.JexlEngine/setFunctions
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

    public Object eval(String expr) {
        return eval(expr, null);
    }

    @Override
    public Object eval(String expr, Map<String, Object> params) {
        String expression = (null == expr || expr.isBlank()) ? null : expr.trim();
        if (null == expression || !EXPR_PATTERN.matcher(expression).find()) {
            return expression;
        }
        MapContext context = new MapContext();
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
        String temp = expression.substring(2, expression.length() - 1);
        return engine.createExpression(temp).evaluate(context);
    }
}
