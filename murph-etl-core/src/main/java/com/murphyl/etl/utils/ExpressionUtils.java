package com.murphyl.etl.utils;

import com.murphyl.etl.support.expression.DataSourceUdf;
import com.murphyl.etl.support.expression.PreparedUdf;
import org.apache.commons.jexl3.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 表达式 - 工具类
 *
 * @date: 2021/12/20 10:10
 * @author: murph
 */
public final class ExpressionUtils {

    private static final JexlEngine JEXL_ENGINE;
    private static final JxltEngine JXLT_ENGINE;
    private static final JexlContext EVAL_CONTEXT;

    static {
        // UDF
        Map<String, Object> UDF_REGISTER = new HashMap<>(2);
        UDF_REGISTER.put(null, new PreparedUdf());
        UDF_REGISTER.put("datasource", new DataSourceUdf());
        // ENGINE
        JEXL_ENGINE = new JexlBuilder().namespaces(UDF_REGISTER).create();
        JXLT_ENGINE = JEXL_ENGINE.createJxltEngine(true);

        EVAL_CONTEXT = new MapContext();
    }

    public static <T> T evalJxtl(String expression) {
        return (T) JXLT_ENGINE.createExpression(expression).evaluate(EVAL_CONTEXT);
    }

    public static <T> T evalJxtl(String expression, Map<String, Object> params) {
        JexlContext context = new MapContext(params);
        return (T) JXLT_ENGINE.createExpression(expression).evaluate(context);
    }

    public static <T> T evalJexl(String expression, Map<String, Object> params) {
        JexlContext context = new MapContext(params);
        return (T) JEXL_ENGINE.createExpression(expression).evaluate(context);
    }

}
