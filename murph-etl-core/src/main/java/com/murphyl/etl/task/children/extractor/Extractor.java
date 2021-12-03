package com.murphyl.etl.task.children.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Feature;

import java.util.Map;
import java.util.Properties;

/**
 * ETL - Extract 门面
 *
 * @date: 2021/12/2 15:49
 * @author: murph
 */
public interface Extractor extends Feature {

    /**
     * 抽取数据
     * @param dsl
     * @param jobParams
     * @param stepProps
     * @return
     */
    Dataframe extract(String dsl, Map<String, Object> jobParams, Properties stepProps);

}
