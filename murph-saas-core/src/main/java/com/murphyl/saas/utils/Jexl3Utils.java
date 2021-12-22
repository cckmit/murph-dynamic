package com.murphyl.saas.utils;

import com.murphyl.saas.support.jexl3.PreparedUdf;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JxltEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * JEXL3 - 工具类
 *
 * @author: murph
 * @date: 2021/12/23 - 1:46
 */
public class Jexl3Utils {

    private static final JexlEngine JEXL_ENGINE;
    private static final JxltEngine JXLT_ENGINE;

    static {
        Map<String, Object> UDF_REGISTER = new TreeMap<>();
        UDF_REGISTER.put(null, new PreparedUdf());
        JEXL_ENGINE = new JexlBuilder().charset(StandardCharsets.UTF_8).namespaces(UDF_REGISTER).create();

        JXLT_ENGINE = JEXL_ENGINE.createJxltEngine(false);
    }


}
