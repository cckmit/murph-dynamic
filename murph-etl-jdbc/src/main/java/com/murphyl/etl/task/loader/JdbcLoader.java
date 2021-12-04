package com.murphyl.etl.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;

import java.util.Map;
import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/3 - 23:39
 */
@Qualifier({"jdbc", "sql"})
public class JdbcLoader implements Loader {

    @Override
    public void load(Map<String, Object> jobParams, Dataframe dataframe, Properties props) {

    }

}
