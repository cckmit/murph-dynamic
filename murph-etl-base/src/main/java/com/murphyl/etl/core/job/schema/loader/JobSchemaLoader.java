package com.murphyl.etl.core.job.schema.loader;

import com.murphyl.dynamic.Feature;
import com.murphyl.etl.core.job.schema.JobSchema;

import java.util.List;

/**
 * 任务加载器
 *
 * @date: 2021/11/16 10:36
 * @author: murph
 */
public interface JobSchemaLoader extends Feature {

    /**
     * 加载任务
     *
     * @return
     */
    List<JobSchema> load();

}
