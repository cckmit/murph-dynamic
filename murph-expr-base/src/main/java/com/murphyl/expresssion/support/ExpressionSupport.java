package com.murphyl.expresssion.support;

import com.murphyl.expresssion.core.ExpressionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表达式 - 工具类
 *
 * @date: 2021/12/23 9:43
 * @author: murph
 */
public final class ExpressionSupport {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionSupport.class);

    private static final Map<String, ExpressionEngine> DETECTED_EXPRESSION_ENGINES;

    static {
        DETECTED_EXPRESSION_ENGINES = new ConcurrentHashMap<>();
        ServiceLoader.load(ExpressionEngine.class).forEach(engine -> {
            logger.info("表达式引擎（{}）注册别名：{}", engine, engine.alias());
            for (String alias : engine.alias()) {
                DETECTED_EXPRESSION_ENGINES.put(alias, engine);
            }
        });
    }

    public static Object eval(String expression) {
        return eval(expression, null);
    }

    public static <R> R eval(String expression, Map<String, Object> params, Class<R> tClass) {
        Object result = eval(expression, params);
        if (null == result) {
            return null;
        }
        if (tClass.isInstance(result)) {
            return tClass.cast(result);
        }
        throw new IllegalStateException();
    }

    public static Object eval(String expression, Map<String, Object> params) {
        return null;
    }

    public static Object eval(ExpressionEngine engine, String expression, Map<String, Object> params) {
        Objects.requireNonNull(engine, "脚本表达式引擎不能为空");
        return engine.eval(expression, params);
    }

}
