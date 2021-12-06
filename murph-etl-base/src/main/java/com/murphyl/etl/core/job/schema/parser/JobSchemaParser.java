package com.murphyl.etl.core.job.schema.parser;

import com.murphyl.dynamic.Feature;
import com.murphyl.dynamic.Group;
import com.murphyl.etl.core.job.schema.JobSchema;

/**
 * 配置 - 构造门面
 *
 * @date: 2021/11/11 13:15
 * @author: murph
 */
@Group
public interface JobSchemaParser extends Feature {

    /**
     * 解析配置文件
     *
     * @param unique
     * @return
     */
    JobSchema parse(String unique);


}
