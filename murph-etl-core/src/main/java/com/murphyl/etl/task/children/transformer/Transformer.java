package com.murphyl.etl.task.children.transformer;

import com.murphyl.dataframe.Dataframe;

import java.util.Map;
import java.util.Properties;

/**
 * ETL - Transform 门面
 *
 * @date: 2021/12/2 15:49
 * @author: murph
 */
public interface Transformer {

    /**
     * 转换数据
     *
     * @param jobParams
     * @param dataframe
     * @param props
     * @return
     */
    Dataframe transform(Map<String, Object> jobParams, Dataframe dataframe, Properties props);
}
