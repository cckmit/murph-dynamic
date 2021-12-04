package com.murphyl.etl.jdbc.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.task.loader.Loader;

import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/3 - 23:39
 */
@Qualifier({"jdbc", "sql"})
public class JdbcLoader implements Loader {

    @Override
    public void load(String dsl, Dataframe dataframe, Properties props) {

    }

}
