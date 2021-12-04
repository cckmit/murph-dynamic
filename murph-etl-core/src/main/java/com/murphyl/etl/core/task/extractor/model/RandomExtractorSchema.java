package com.murphyl.etl.core.task.extractor.model;

import java.util.List;
import java.util.Map;

/**
 * RandomExtractor - 模式
 *
 * @date: 2021/12/3 13:11
 * @author: murph
 */
public class RandomExtractorSchema {

    private String engine;
    private List<Column> columns;

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public static class Column {

        private String name;
        private String expr;
        private Map<String, String> extra;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getExpr() {
            return expr;
        }

        public void setExpr(String expr) {
            this.expr = expr;
        }

        public Map<String, String> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, String> extra) {
            this.extra = extra;
        }

    }

}
