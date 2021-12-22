package com.murphyl.saas.support.jexl3;

import com.murphyl.saas.core.AppContext;

/**
 * 表达式 - UDF
 *
 * @author: murph
 * @date: 2021/12/23 - 1:47
 */
public class PreparedUdf {

    public String env(String key) {
        return AppContext.CONFIG.getString(key);
    }

}
