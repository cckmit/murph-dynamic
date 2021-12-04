package com.murphyl.etl.utils;

import com.murphyl.etl.consts.ETL;

import java.util.stream.Stream;

/**
 * -
 * author: murph
 * 2021/12/4 - 11:26
 */
public final class Utils {

    public static String[] mapTaskChildAlias(String role, String[] alias) {
        return Stream.of(alias).map(item -> String.format("%s/%s/%s", ETL.XPATH_OF_ETL_JOB_TASKS, role, item)).toArray(String[]::new);
    }

}
