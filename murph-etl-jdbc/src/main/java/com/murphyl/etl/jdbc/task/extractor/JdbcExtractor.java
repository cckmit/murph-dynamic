package com.murphyl.etl.jdbc.task.extractor;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.task.extractor.Extractor;

import java.util.Properties;

/**
 * -
 * author: murph
 * 2021/12/3 - 23:41
 */
@Qualifier({"jdbc", "sql"})
public class JdbcExtractor implements Extractor {

    @Override
    public Dataframe extract(String dsl, Properties stepProps) {
        return null;
    }

}
