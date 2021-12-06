package com.murphyl.etl.core.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;

import java.util.Properties;

/**
 * ETL - Extract 门面
 *
 * @date: 2021/12/2 15:49
 * @author: murph
 */
@Group
public interface Extractor extends Feature {

    /**
     * 抽取数据
     *
     * @param dsl
     * @param stepProps
     * @return
     */
    Dataframe extract(String dsl, Properties stepProps);

}
