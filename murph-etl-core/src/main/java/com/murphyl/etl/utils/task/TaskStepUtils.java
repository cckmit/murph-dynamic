package com.murphyl.etl.utils.task;

import com.murphyl.etl.support.Environments;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Properties;

/**
 * 子任务 - 批次支持
 *
 * @date: 2021/12/6 18:42
 * @author: murph
 */
public final class TaskStepUtils {

    /**
     * 获取批次信息
     *
     * @param properties
     * @return
     */
    public static Integer getBatchSize(Properties properties) {
        Integer batchSize = NumberUtils.createInteger(properties.getProperty("batchSize"));
        if (null == batchSize) {
            batchSize = Environments.getInt("BATCH_SIZE", 1000);
        }
        return batchSize;
    }

}
