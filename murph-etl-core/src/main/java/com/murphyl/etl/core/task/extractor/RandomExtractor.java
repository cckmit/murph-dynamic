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
        // 结果
        Dataframe result = null;
        for (Integer rowIndex = 0; rowIndex < batchSize; rowIndex++) {
            Map<String, Object> values = ExpressionUtils.evalJexl(dsl, params);
            if(rowIndex == 0) {
                String[] headers = values.keySet().toArray(String[]::new);
                result = new Dataframe(headers, batchSize);
            }
            for (int columnIndex = 0; columnIndex < result.getHeaders().length; columnIndex++) {
                result.setValue(rowIndex, columnIndex, values.get(result.getHeaders()[columnIndex]));
            }
        }
        logger.info("create {} row random data: {}", batchSize, result.getHeaders());
        return result;
    }

}
