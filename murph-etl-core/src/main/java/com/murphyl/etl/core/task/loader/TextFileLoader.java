package com.murphyl.etl.core.task.loader;

import com.murphyl.dataframe.Dataframe;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.utils.TaskStepUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文本文件 - Loader
 *
 * @date: 2021/12/2 15:55
 * @author: murph
 */
@Qualifier({"text-file", "file", "csv"})
public class TextFileLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(TextFileLoader.class);

    private static final String COL_WRAPPER = "\"%s\"";

    private static final String SEPARATOR = ",";

    @Override
    public void load(String dsl, Dataframe dataframe, Map<String, Object> stepProps) {
        Integer batchSize = TaskStepUtils.getBatchSize(stepProps);
        Path target = Paths.get(TaskStepUtils.get(stepProps, "target"));
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        try {
            if (!Files.exists(target)) {
                Files.createFile(target);
            }
            for (int i = 1; i < dataframe.height(); i++) {
                joiner.add(renderLine(dataframe.row(i - 1)));
                if (i % batchSize == 0) {
                    Files.write(target, joiner.toString().getBytes(), StandardOpenOption.APPEND);
                    joiner = new StringJoiner(System.lineSeparator());
                }
            }
            if (joiner.length() > 0) {
                Files.write(target, joiner.toString().getBytes(), StandardOpenOption.APPEND);
            }
        } catch (Exception e) {
            logger.error("write text file error", e);
        }

    }

    private String renderLine(Map<String, Object> values) {
        return Stream.of(values).map(col -> {
            if (null == col) {
                return StringUtils.EMPTY;
            } else {
                return String.format(COL_WRAPPER, StringEscapeUtils.escapeJava(col.toString()));
            }
        }).collect(Collectors.joining(SEPARATOR)).trim();
    }

}
