package com.murphyl.etl.support;

import com.murphyl.expr.core.ExpressionEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * 表达式执行引擎 - 支持
 *
 * @date: 2021/12/6 18:53
 * @author: murph
 */
public interface ExpressionSupport {

    /**
     * 获取表达式执行引擎
     *
     * @param engineName
     * @return
     */
    default ExpressionEvaluator getEngine(String engineName) {
        if (StringUtils.isBlank(engineName)) {
            return getDefaultEngine();
        }
        ExpressionEvaluator engine = Environments.getFeature(ExpressionEvaluator.class, engineName);
        Objects.requireNonNull(engine, "no matched expression evaluate engine: " + engineName);
        return engine;
    }

    /**
     * 获取默认表达式执行引擎
     *
     * @return
     */
    default ExpressionEvaluator getDefaultEngine() {
        String name = Environments.get("EXPRESSION_ENGINE");
        Validate.notBlank(name, "please set expression evaluate engine in .env file use key: EXPRESSION_ENGINE");
        ExpressionEvaluator engine = Environments.getFeature(ExpressionEvaluator.class, name);
        Objects.requireNonNull(engine, "no default expression evaluate engine: " + name);
        return engine;
    }

}
