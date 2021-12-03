package com.murphyl.etl.task.children.extractor.impl;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Group;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.task.children.extractor.Extractor;

import java.util.Map;
import java.util.Properties;

/**
 * JDBC - Extractor
 *
 * @date: 2021/12/2 15:51
 * @author: murph
 */
@Group(Extractor.class)
@Qualifier({"jdbc", "sql"})
public class JdbcExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Map<String, Object> jobParams, Properties stepProps) {
        return new Dataframe(0, 0);
    }
}
