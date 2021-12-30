package com.murphyl.saas.support.resource.udf;

import com.murphyl.expresssion.core.UdfSourceAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表达式 - UDF
 *
 * @author: murph
 * @date: 2021/12/23 - 1:47
 */
public class SaasUdfSource implements UdfSourceAssembly {

    private static final Logger logger = LoggerFactory.getLogger(SaasUdfSource.class);

    public Object get(String url) {
        return "hello: " + url;
    }

}
