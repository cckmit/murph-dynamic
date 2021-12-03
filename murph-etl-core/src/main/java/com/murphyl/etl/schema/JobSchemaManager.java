package com.murphyl.etl.schema;

import com.murphyl.etl.job.JobSchema;
import com.murphyl.etl.schema.parser.JobSchemaParser;
import com.murphyl.etl.support.Environments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 任务配置解析器
 *
 * @date: 2021/12/1 11:43
 * @author: murph
 */
public final class JobSchemaManager {

    private static final Logger logger = LoggerFactory.getLogger(JobSchemaManager.class);

    private static final Map<String, JobSchemaParser> REGISTERED_SCHEMA_PARSERS;

    static {
        REGISTERED_SCHEMA_PARSERS = Environments.loadService("job schema parser", JobSchemaParser.class);
    }

    public static JobSchema parse(String filepath) {
        int dotIndex = filepath.lastIndexOf('.');
        String ext = (dotIndex == -1) ? "" : filepath.substring(dotIndex + 1);
        JobSchemaParser parser = REGISTERED_SCHEMA_PARSERS.get(ext);
        if (null == parser) {
            throw new IllegalStateException("schema parser not found: " + ext);
        }
        logger.info("use [{}] parse file ({})", parser, filepath);
        return parser.parse(filepath);
    }

}
