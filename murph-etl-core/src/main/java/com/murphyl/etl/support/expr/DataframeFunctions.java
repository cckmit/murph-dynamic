package com.murphyl.etl.support.expr;

import com.murphyl.expr.core.UserDefinedFunction;

import java.util.UUID;

/**
 * -
 * author: murph
 * 2021/12/4 - 12:59
 */
public class DataframeFunctions implements UserDefinedFunction {

    @Override
    public String namespace() {
        return "dataframe";
    }

    public Object column(String name) {
        return String.format("%s:%s", name, UUID.randomUUID());
    }

    public Object col(String name) {
        return column(name);
    }

}
