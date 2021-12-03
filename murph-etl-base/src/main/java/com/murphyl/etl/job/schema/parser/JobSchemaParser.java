package com.murphyl.etl.job.schema.parser;

import com.murphyl.dynamic.Feature;
import com.murphyl.etl.job.schema.JobSchema;

/**
 * 配置 - 构造门面
 *
 * @date: 2021/11/11 13:15
 * @author: murph
 */
public interface JobSchemaParser extends Feature {

    /**
     * 解析配置文件
     *
     * @param unique
     * @return
     */
    JobSchema parse(String unique);


}
