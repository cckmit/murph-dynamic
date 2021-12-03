package com.murphyl.expr.core;

import java.util.*;

/**
 * 表达式执行器 - 构造器
 *
 * @date: 2021/12/3 14:00
 * @author: murph
 */
public final class ExpressionEvaluatorBuilder {

    private ExpressionEvaluator evaluator;

    private Map<String, Class> registeredUdf;


    public ExpressionEvaluatorBuilder engine(String name) {
        this.evaluator = getExprEvaluator(name);
        return this;
    }

    public ExpressionEvaluatorBuilder udf(String namespace, Class udfClass) {
        if(null == this.registeredUdf) {
            this.registeredUdf = new TreeMap<>();
        }
        this.registeredUdf.put(namespace, udfClass);
        return this;
    }

    public ExpressionEvaluatorBuilder udfMap(Map<String, Class> udfMapper) {
        if(null == this.registeredUdf) {
            this.registeredUdf = new TreeMap<>();
        }
        this.registeredUdf.putAll(udfMapper);
        return this;
    }

    public ExpressionEvaluator build() {
        this.evaluator.setUdf(registeredUdf);
        return this.evaluator;
    }

    private ExpressionEvaluator getExprEvaluator(String resolver) {
        Iterator<ExpressionEvaluator> evIterator = ServiceLoader.load(ExpressionEvaluator.class).iterator();
        ExpressionEvaluator evaluator;
        while (evIterator.hasNext()) {
            evaluator = evIterator.next();
            if (Arrays.asList(evaluator.getAlias()).contains(resolver)) {
                return evaluator;
            }
        }
        throw new IllegalStateException("no matched expression evaluator: " + resolver);
    }

}
