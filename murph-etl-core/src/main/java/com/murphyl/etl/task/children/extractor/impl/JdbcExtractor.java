package com.murphyl.etl.task.children.extractor.impl;

import com.murphyl.etl.core.Dataframe;
import com.murphyl.etl.task.children.extractor.Extractor;
import com.murphyl.etl.support.Qualifier;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * JDBC - Extractor
 *
 * @date: 2021/12/2 15:51
 * @author: murph
 */
@Qualifier({ "jdbc", "sql" })
public class JdbcExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Map<String, Object> jobParams, Properties stepProps) {
        return new Dataframe(0, 0);
    }
}
