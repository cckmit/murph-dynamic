package com.murphyl.etl.job.schema.loader;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.job.schema.JobSchema;

import java.util.List;

/**
 * -
 * author: murph
 * 2021/12/3 - 23:49
 */
@Qualifier({"jdbc", "sql"})
public class JdbcJobSchemaLoader implements JobSchemaLoader {

    @Override
    public List<JobSchema> load() {
        return null;
    }

}
