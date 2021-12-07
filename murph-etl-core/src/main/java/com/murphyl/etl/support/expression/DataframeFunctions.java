package com.murphyl.etl.support.expression;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.expr.core.MurphExprUdf;

import java.util.UUID;

/**
 * -
 * 2021/12/4 - 12:59
 * @author murph
 */
@Qualifier("dataframe")
public class DataframeFunctions implements MurphExprUdf {

    public Object column(String name) {
        return String.format("%s:%s", name, UUID.randomUUID());
    }

    public Object col(String name) {
        return column(name);
    }

}
