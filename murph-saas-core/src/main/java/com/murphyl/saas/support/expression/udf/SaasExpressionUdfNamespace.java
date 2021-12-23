package com.murphyl.saas.support.expression.udf;

import com.murphyl.expresssion.core.ExpressionUdfNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表达式 - UDF
 *
 * @author: murph
 * @date: 2021/12/23 - 1:47
 */
public class SaasExpressionUdfNamespace implements ExpressionUdfNamespace {

    private static final Logger logger = LoggerFactory.getLogger(SaasExpressionUdfNamespace.class);

    public SaasExpressionUdfNamespace() {
        logger.info("正在加载 SAAS 模块内置的表达式 UDF……");
    }

    public Object get(String url) {
        return "hello: " + url;
    }

}
