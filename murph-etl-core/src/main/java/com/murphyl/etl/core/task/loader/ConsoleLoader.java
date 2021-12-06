package com.murphyl.etl.core.task.loader;

import com.google.common.base.Joiner;
import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.core.task.BatchSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 控制台 - Loader
 * - https://github.com/vdmeer/asciitable
 * - http://www.vandermeer.de/projects/skb/java/asciitable/examples/AT_07c_LongestLine.html
 *
 * @date: 2021/12/2 15:56
 * @author: murph
 */
@Qualifier({"console", "stdout", "logger", "system.out"})
public class ConsoleLoader implements Loader, BatchSupport {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLoader.class);

    @Override
    public void load(String dsl, Dataframe dataframe, Properties stepProps) {
        Integer batchSize = getBatchSize(stepProps);
        Joiner lineJoiner = Joiner.on(stepProps.getProperty("separator", "\t")).useForNull("");
        StringBuilder builder;
        int from, to, rowIndex = 0;
        for (int batchNo = 0; batchNo < dataframe.height() && rowIndex < dataframe.height(); batchNo++) {
            builder = new StringBuilder();
            if (null != dataframe.getHeaders()) {
                builder.append('[').append(lineJoiner.join(dataframe.getHeaders())).append(']');
            }
            from = batchNo * batchSize;
            to = (batchNo + 1) * batchSize;
            for (rowIndex = from; rowIndex < to && rowIndex < dataframe.height(); rowIndex++) {
                if (builder.length() > 0) {
                    builder.append(System.lineSeparator());
                }
                builder.append(lineJoiner.join(dataframe.row(rowIndex)));
            }
            logger.info("show data from {} to {}: \n{}", from, to, builder);
        }
    }

}
