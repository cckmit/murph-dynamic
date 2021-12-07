package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dataframe.support.AsciiTable;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.utils.TaskStepUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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
public class ConsoleLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLoader.class);

    @Override
    public void load(String dsl, Dataframe dataframe, Map<String, Object> stepProps) {
        Integer batchSize = TaskStepUtils.getBatchSize(stepProps);
        AsciiTable at = new AsciiTable(dataframe);
        int from, to;
        for (int batchNo = 0; batchSize * batchNo < dataframe.height(); batchNo++) {
            from = batchNo * batchSize;
            to = (batchNo + 1) * batchSize;
            logger.info("show dataframe from {} to {}: \n{}", from, to, at.render(from, to));
        }
    }

}
