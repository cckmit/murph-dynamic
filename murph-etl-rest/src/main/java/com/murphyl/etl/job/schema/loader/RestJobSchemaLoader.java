package com.murphyl.etl.job.schema.loader;

import com.murphyl.dynamic.Group;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.job.schema.JobSchema;

import java.util.List;

/**
 * -
 * author: murph
 * 2021/12/3 - 23:58
 */
@Group(JobSchemaLoader.class)
@Qualifier({ "rest", "http" })
public class RestJobSchemaLoader implements JobSchemaLoader {

    @Override
    public List<JobSchema> load() {
        return null;
    }
}
