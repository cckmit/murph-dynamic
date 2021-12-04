package com.murphyl.etl.rest.job.schema.loader;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.job.schema.JobSchema;
import com.murphyl.etl.core.job.schema.loader.JobSchemaLoader;

import java.util.List;

/**
 * -
 * author: murph
 * 2021/12/3 - 23:58
 */
@Qualifier({ "rest", "http" })
public class RestJobSchemaLoader implements JobSchemaLoader {

    @Override
    public List<JobSchema> load() {
        return null;
    }
}
