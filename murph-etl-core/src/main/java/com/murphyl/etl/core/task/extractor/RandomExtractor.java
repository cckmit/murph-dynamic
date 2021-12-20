package com.murphyl.etl.core.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.task.extractor.model.RandomExtractorSchema;
import com.murphyl.etl.utils.ExpressionUtils;
import com.murphyl.etl.utils.Serializers;
import com.murphyl.etl.utils.TaskStepUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 随机数据 - Extractor
 *
 * @date: 2021/12/2 15:53
 * @author: murph
 */
@Qualifier({"random"})
public class RandomExtractor implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(RandomExtractor.class);

    @Override
    public Dataframe extract(String dsl, Map<String, Object> params) {
        Integer batchSize = TaskStepUtils.getBatchSize(params);
        RandomExtractorSchema schema = Serializers.JSON.parse(dsl, RandomExtractorSchema.class);
        if (null == schema || null == schema.getColumns() || schema.getColumns().isEmpty()) {
            throw new IllegalStateException("extractor schema lost columns");
        }
        // 列配置
        List<RandomExtractorSchema.Column> columns = schema.getColumns();
        // 数据帧标题
        String[] headers = columns.stream().map(col -> col.getName()).toArray(String[]::new);
        // 结果
        Dataframe result = new Dataframe(headers, batchSize);
        Object value;
        RandomExtractorSchema.Column column;
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            // 列配置信息
            column = columns.get(columnIndex);
            // 生成数据
            for (int rowIndex = 0; rowIndex < batchSize; rowIndex++) {
                // 计算表达式值
                value = ExpressionUtils.eval(column.getExpr(), params);
                // 写二维表
                result.setValue(rowIndex, columnIndex, value);
            }
        }
        return result;
    }

}
