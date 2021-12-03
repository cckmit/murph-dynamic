package com.murphyl.etl.task.children.transformer.impl;

import com.murphyl.etl.core.Dataframe;
import com.murphyl.etl.support.Qualifier;
import com.murphyl.etl.task.children.transformer.Transformer;

import java.util.Map;
import java.util.Properties;

/**
 * 逻辑过滤器 - Transformer
 *
 * @date: 2021/12/2 16:17
 * @author: murph
 */
@Qualifier({ "and", "or" })
public class LogicFilterTransformer implements Transformer {

    @Override
    public Dataframe transform(Map<String, Object> jobParams, Dataframe dataframe, Properties props) {
        return dataframe;
    }

}
