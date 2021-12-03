package com.murphyl.etl.task.children.loader.impl;

import com.google.common.primitives.Ints;
import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Group;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.task.children.loader.Loader;
import de.vandermeer.asciitable.*;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * 控制台 - Loader
 *
 * @date: 2021/12/2 15:56
 * @author: murph
 */
@Group(Loader.class)
@Qualifier({"console", "stdout", "logger", "system.out"})
public class ConsoleLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleLoader.class);

    @Override
    public void load(Map<String, Object> jobParams, Dataframe dataframe, Properties stepProps) {
        Integer batchSize = Ints.tryParse(stepProps.getProperty("batchSize", "1000"));
        if (null == batchSize) {
            logger.warn("batchSize({}) not number", batchSize);
            batchSize = 10;
        }
        AsciiTable at;
        CWC_LongestLine cwc = new CWC_LongestLine();
        int from, to, rowIndex = 0;
        for (int batchNo = 0; batchNo < dataframe.height() && rowIndex < dataframe.height(); batchNo++) {
            at = new AsciiTable();
            at.getRenderer().setCWC(cwc);
            if (null != dataframe.getHeaders()) {
                at.addRule();
                at.addRow(dataframe.getHeaders()).setTextAlignment(TextAlignment.CENTER);
                at.addRule();
            }
            from = batchNo * batchSize;
            to = (batchNo + 1) * batchSize;
            for (rowIndex = from; rowIndex < to && rowIndex < dataframe.height(); rowIndex++) {
                at.addRow(dataframe.row(rowIndex));
                at.addRule();
            }
            logger.info("show data from {} to {}: \n{}", from, to, at.render());
        }
    }

}
