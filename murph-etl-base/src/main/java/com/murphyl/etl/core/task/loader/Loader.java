package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;

import java.util.Map;
import java.util.Properties;

/**
 * ETL - Load 门面
 *
 * @date: 2021/12/2 15:50
 * @author: murph
 */
@Group
public interface Loader extends Feature {

    /**
     * 数据载入
     *
     * @param dsl
     * @param dataframe
     * @param params
     * @return
     */
    void load(String dsl, Dataframe dataframe, Map<String, Object> params);

}
