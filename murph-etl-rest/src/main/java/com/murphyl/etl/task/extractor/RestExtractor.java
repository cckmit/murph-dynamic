package com.murphyl.etl.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;

import java.util.Map;
import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/4 - 0:00
 */
@Qualifier({ "rest", "http" })
public class RestExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Map<String, Object> jobParams, Properties stepProps) {
        return null;
    }
}
