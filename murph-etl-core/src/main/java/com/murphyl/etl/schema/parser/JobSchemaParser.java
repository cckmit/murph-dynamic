package com.murphyl.etl.schema.parser;

import com.murphyl.etl.job.JobSchema;

/**
 * 配置 - 构造门面
 *
 * @date: 2021/11/11 13:15
 * @author: murph
 */
public interface JobSchemaParser {

    /**
     * 解析配置文件
     *
     * @param unique
     * @return
     */
    JobSchema parse(String unique);


}
