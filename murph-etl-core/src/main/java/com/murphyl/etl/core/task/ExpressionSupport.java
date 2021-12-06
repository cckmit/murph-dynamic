package com.murphyl.etl.core.task;

import com.murphyl.etl.support.Environments;
import com.murphyl.expr.core.ExpressionEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Objects;

/**
 * 脚本 - 支持
 *
 * @date: 2021/12/6 18:53
 * @author: murph
 */
public interface ExpressionSupport {

    /**
     * 获取脚本引擎
     *
     * @param engineName
     * @return
     */
    default ExpressionEvaluator getEngine(String engineName) {
        String name = StringUtils.isEmpty(engineName) ? Environments.get("EXPRESSION_ENGINE") : engineName;
        Validate.notBlank(engineName, "please set expression engine use task step attribute(engine) or .env file(EXPRESSION_ENGINE)");
        ExpressionEvaluator engine = Environments.getFeature(ExpressionEvaluator.class, name);
        Objects.requireNonNull(engine, "no matched expression eval engine: " + name);
        return engine;
    }

}
