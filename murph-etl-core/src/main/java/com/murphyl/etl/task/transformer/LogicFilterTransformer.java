package com.murphyl.etl.task.transformer;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;

import java.util.Map;
import java.util.Properties;

/**
 * 逻辑过滤器 - Transformer
 *
 * @date: 2021/12/2 16:17
 * @author: murph
 */
@Qualifier({ "filter", "passBy" })
public class LogicFilterTransformer implements Transformer {

    @Override
    public Dataframe transform(Map<String, Object> jobParams, Dataframe dataframe, Properties props) {
        return dataframe;
    }

}
