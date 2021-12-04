package com.murphyl.etl.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;

import java.util.Map;
import java.util.Properties;

/**
 * 文本文件 - Extractor
 *
 * @date: 2021/12/2 15:53
 * @author: murph
 */
@Qualifier({"text-file", "file", "csv"})
public class TextFileExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Map<String, Object> jobParams, Properties stepProps) {
        return new Dataframe(new String[0], 0);
    }

}
