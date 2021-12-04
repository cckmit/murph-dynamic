package com.murphyl.etl.consts;

/**
 * -
 * author: murph
 * 2021/12/4 - 9:42
 */
public final class ETL {

    public static final String XPATH_OF_ETL_JOB = "/etl-job";

    public static final String XPATH_OF_ETL_JOB_PARAM = String.format("%s/params/assign", XPATH_OF_ETL_JOB);

    public static final String XPATH_OF_ETL_JOB_TASKS = String.format("%s/task", XPATH_OF_ETL_JOB);

    /**
     * extractor
     */
    public static final String TASK_ROLE_EXTRACTOR = "extractor";

    /**
     * transformer
     */
    public static final String TASK_ROLE_TRANSFORMER = "transformer";

    /**
     * loader
     */
    public static final String TASK_ROLE_LOADER = "loader";



}
