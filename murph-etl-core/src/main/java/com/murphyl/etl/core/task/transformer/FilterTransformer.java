package com.murphyl.etl.core.task.transformer;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.support.expr.ExpressionSupport;
import com.murphyl.expr.core.ExpressionEvaluator;

import java.util.Arrays;
import java.util.Properties;

/**
 * 逻辑过滤器 - Transformer
 *
 * @date: 2021/12/2 16:17
 * @author: murph
 */
@Qualifier({"filter"})
public class FilterTransformer implements Transformer, ExpressionSupport {

    @Override
    public Dataframe transform(String dsl, Dataframe dataframe, Properties stepProps) {
        String engineName = stepProps.getProperty("engine");
        ExpressionEvaluator expressionEvaluator = getEngine(engineName);
        for (int rowIndex = 0; rowIndex < dataframe.height(); rowIndex++) {
            System.out.println(expressionEvaluator.eval(dsl) + " - " + Arrays.toString(dataframe.row(rowIndex)));
        }
        return dataframe;
    }

}
