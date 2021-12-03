package com.murphyl.etl.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Group;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.task.extractor.Extractor;

import java.util.Map;
import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/4 - 0:00
 */
@Group(Loader.class)
@Qualifier({ "rest", "http" })
public class RestLoader implements Loader {

    @Override
    public void load(Map<String, Object> jobParams, Dataframe dataframe, Properties props) {

    }

}
