package com.murphyl.expr.jexl2;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.expr.core.ExpressionEvaluator;
import com.murphyl.expr.core.MurphExprUdf;
import com.murphyl.expr.support.PreparedExpressions;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

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

    private final JexlEngine engine;

    public Jexl2Evaluator() {
        this.engine = new JexlEngine();
        Map<String, Object> functions = new HashMap<>();
        functions.put(null, PreparedExpressions.class);
        Iterator<MurphExprUdf> udfIterator = ServiceLoader.load(MurphExprUdf.class).iterator();
        MurphExprUdf udf;
        while (udfIterator.hasNext()) {
            udf = udfIterator.next();
            if (null == udf.alias() || udf.alias().length == 0) {
                continue;
            }
            for (String alias : udf.alias()) {
                functions.put(alias, udf);
            }
        }
        this.engine.setFunctions(functions);
    }

    @Override
    public Object eval(String expr) {
        return eval(expr, null, true);
    }

    @Override
    public Object eval(String expr, Map<String, Object> params, boolean clean) {
        String expression = clean ? clean(expr) : expr;
        if (null == expression) {
            return expr;
        }
        MapContext context = new MapContext();
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
        return engine.createExpression(expression).evaluate(context);
    }

}
