package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Feature;

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
     * @param dsl
     * @param dataframe
     * @param props
     * @return
     */
    void load(String dsl, Dataframe dataframe, Properties props);

}