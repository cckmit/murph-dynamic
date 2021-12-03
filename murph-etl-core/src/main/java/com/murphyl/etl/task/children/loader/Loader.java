package com.murphyl.etl.task.children.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Feature;

import java.util.Map;
import java.util.Properties;

/**
 * ETL - Load 门面
 *
 * @date: 2021/12/2 15:50
 * @author: murph
 */
public interface Loader extends Feature {

    /**
     * 数据载入
     *
     * @param jobParams
     * @param dataframe
     * @param props
     * @return
     */
    void load(Map<String, Object> jobParams, Dataframe dataframe, Properties props);

}
