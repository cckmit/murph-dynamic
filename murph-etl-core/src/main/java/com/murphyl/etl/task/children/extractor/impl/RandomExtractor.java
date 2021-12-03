package com.murphyl.etl.task.children.extractor.impl;

import com.google.common.primitives.Ints;
import com.murphyl.etl.core.Dataframe;
import com.murphyl.etl.support.Environments;
import com.murphyl.etl.support.Qualifier;
import com.murphyl.etl.task.children.extractor.Extractor;
import com.murphyl.etl.task.children.extractor.model.RandomExtractorSchema;
import com.murphyl.etl.utils.Serializers;
import com.murphyl.expr.core.ExpressionEvaluator;
import com.networknt.schema.JsonSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 随机数据 - Extractor
 *
 * @date: 2021/12/2 15:53
 * @author: murph
 */
@Qualifier("random")
public class RandomExtractor implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(RandomExtractor.class);

    private static final JsonSchema VALIDATE_SCHEMA;

    static {
        String schemaFile = "META-INF/schemas/etl-task-extractor-random-schema.json";
        VALIDATE_SCHEMA = Environments.getJsonSchema(schemaFile);
    }

    @Override
    public Dataframe extract(String dsl, Map<String, Object> jobParams, Properties stepProps) {
        Integer batchSize = Ints.tryParse(stepProps.getProperty("batchSize", "1000"));
        if (null == batchSize) {
            logger.warn("batchSize({}) not number", batchSize);
            batchSize = 10;
        }
        RandomExtractorSchema schema = Serializers.JSON.validateAndParse(VALIDATE_SCHEMA, dsl, RandomExtractorSchema.class);
        if (null == schema || null == schema.getColumns() || schema.getColumns().isEmpty()) {
            throw new IllegalStateException("extractor schema lost columns");
        }
        // 表达式处理起
        ExpressionEvaluator expressionEvaluator = Environments.getExprEvaluator(schema.getEngine());
        logger.info("use [{}] expression evaluator generate {} rows random data", expressionEvaluator, batchSize);
        // 列配置
        List<RandomExtractorSchema.Column> columns = schema.getColumns();
        // 结果
        Dataframe result = new Dataframe(columns.size(), batchSize);
        // 数据帧标题
        result.setHeaders(columns.stream().map(col -> col.getName()).toArray(String[]::new));
        Object value;
        Map<String, Object> extra;
        RandomExtractorSchema.Column column;
        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            // 列配置信息
            column = columns.get(columnIndex);
            // 创建临时值数据池
            extra = getExtraMap(expressionEvaluator, column.getExtra());
            // 生成数据
            for (int rowIndex = 0; rowIndex < batchSize; rowIndex++) {
                // 计算表达式值
                value = expressionEvaluator.eval(column.getExpr(), extra);
                // 写二维表
                result.setValue(rowIndex, columnIndex, value);
            }
        }
        return result;
    }

    private Map<String, Object> getExtraMap(ExpressionEvaluator expressionEvaluator, Map<String, String> extra) {
        if (null == extra || extra.isEmpty()) {
            return null;
        }
        Map<String, Object> resolved = new HashMap<>(extra.size());
        for (Map.Entry<String, String> entry : extra.entrySet()) {
            resolved.put(entry.getKey(), expressionEvaluator.eval(entry.getValue()));
        }
        return resolved;
    }

}
