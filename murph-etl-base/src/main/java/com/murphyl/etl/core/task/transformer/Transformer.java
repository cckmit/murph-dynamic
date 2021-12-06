package com.murphyl.etl.core.task.transformer;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;

import java.util.Properties;

/**
 * ETL - Transform 门面
 *
 * @date: 2021/12/2 15:49
 * @author: murph
 */
@Group
public interface Transformer extends Feature {

    /**
     * 转换数据
     *
     * @param dsl
     * @param dataframe
     * @param props
     * @return
     */
    Dataframe transform(String dsl, Dataframe dataframe, Properties props);
}
