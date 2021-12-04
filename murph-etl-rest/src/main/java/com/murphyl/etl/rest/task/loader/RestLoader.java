package com.murphyl.etl.rest.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.task.loader.Loader;

import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/4 - 0:00
 */
@Qualifier({ "rest", "http" })
public class RestLoader implements Loader {

    @Override
    public void load(String dsl, Dataframe dataframe, Properties props) {

    }

}
