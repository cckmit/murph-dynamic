package com.murphyl.etl.rest.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.task.extractor.Extractor;

import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/4 - 0:00
 */
@Qualifier({ "rest", "http" })
public class RestExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Properties stepProps) {
        return null;
    }
}
