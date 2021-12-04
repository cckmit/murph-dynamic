package com.murphyl.etl.job.schema;

import com.murphyl.etl.job.schema.parser.JobSchemaParser;
import com.murphyl.etl.consts.Environments;
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

    public static JobSchema parse(String filepath) {
        int dotIndex = filepath.lastIndexOf('.');
        String ext = (dotIndex == -1) ? "" : filepath.substring(dotIndex + 1);
        JobSchemaParser parser = Environments.getFeature(JobSchemaParser.class, ext);
        if (null == parser) {
            throw new IllegalStateException("schema parser not found: " + ext);
        }
        logger.info("use [{}] parse file ({})", parser, filepath);
        return parser.parse(filepath);
    }

}
