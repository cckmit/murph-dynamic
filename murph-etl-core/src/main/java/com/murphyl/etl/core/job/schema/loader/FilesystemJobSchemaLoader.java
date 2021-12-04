package com.murphyl.etl.core.job.schema.loader;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.job.schema.JobSchema;

import java.util.List;

/**
 * 基于文件系统的 - 任务加载器
 *
 * @date: 2021/11/16 10:39
 * @author: murph
 */
@Qualifier({"filesystem", "fs"})
public class FilesystemJobSchemaLoader implements JobSchemaLoader {

    @Override
    public List<JobSchema> load() {
        return null;
    }

}
