package com.murphyl.etl.jdbc.job.schema.loader;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.job.schema.JobSchema;
import com.murphyl.etl.core.job.schema.loader.JobSchemaLoader;

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
