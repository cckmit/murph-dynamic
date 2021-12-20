package com.murphyl.etl.core.task.transformer;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;

import java.util.Map;

/**
 * 逻辑过滤器 - Transformer
 *
 * @date: 2021/12/2 16:17
 * @author: murph
 */
@Qualifier({"filter"})
public class FilterTransformer implements Transformer {

    @Override
    public Dataframe transform(String dsl, Dataframe dataframe, Map<String, Object> stepProps) {
        for (int rowIndex = 0; rowIndex < dataframe.height(); rowIndex++) {
            System.out.println(dsl + " - " + dataframe.row(rowIndex));
        }
        return dataframe;
    }

}
