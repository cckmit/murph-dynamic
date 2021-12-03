package com.murphyl.etl.task.children.loader.impl;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.primitives.Ints;
import com.murphyl.dataframe.Dataframe;
import com.murphyl.etl.support.Qualifier;
import com.murphyl.etl.task.children.loader.Loader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
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
@Qualifier({"text-file", "text", "csv"})
public class TextFileLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(TextFileLoader.class);

    private static final Escaper TEXT_ESCAPER;

    private static final String COL_WRAPPER = "\"%s\"";

    private static final String SEPARATOR = ",";

    static {
        TEXT_ESCAPER = Escapers.builder().addEscape('"', "\\\"").build();
    }

    @Override
    public void load(Map<String, Object> jobParams, Dataframe dataframe, Properties stepProps) {
        Integer batchSize = Ints.tryParse(stepProps.getProperty("batchSize", "1000"));
        if (null == batchSize) {
            logger.warn("batchSize({}) not number", batchSize);
            batchSize = 10;
        }
        Path target = Paths.get(stepProps.getProperty("target", String.format("/%s.csv", UUID.randomUUID())));
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add(renderLine(dataframe.getHeaders()));
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

    private String renderLine(Object[] values) {
        return Stream.of(values).map(col -> {
            if (null == col) {
                return StringUtils.EMPTY;
            } else {
                return String.format(COL_WRAPPER, TEXT_ESCAPER.escape(col.toString()));
            }
        }).collect(Collectors.joining(SEPARATOR)).trim();
    }

}
