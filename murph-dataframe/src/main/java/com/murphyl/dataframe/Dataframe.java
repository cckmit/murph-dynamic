package com.murphyl.dataframe;

import java.util.*;

/**
 * 数据帧
 *
 * @date: 2021/12/2 18:54
 * @author: murph
 */
public class Dataframe {

    private String[] headers;
    private final Object[][] values;

    private Map<String, Integer> fieldsMapping;

    public Dataframe(String[] headers, int height) {
        this.headers = headers;
        this.fieldsMapping = new HashMap<>(headers.length);
        for (int i = 0; i < headers.length; i++) {
            this.fieldsMapping.put(headers[i], i);
        }
        this.values = new Object[height][headers.length];
    }

    public Dataframe(String[] headers, Object[][] payload) {
        this.headers = headers;
        this.values = payload;
    }

    public Dataframe(String[] headers, List<Object[]> payload) {
        this.headers = headers;
        this.values = payload.toArray(Object[][]::new);
    }

    public String[] getHeaders() {
        return headers;
    }

    public Object[][] getValues() {
        return values;
    }

    public void setValue(int rowIndex, int columnIndex, Object value) {
        values[rowIndex][columnIndex] = value;
    }


    public int width() {
        return headers.length;
    }

    public int height() {
        return values.length;
    }

    public Map<String, Object> row(int rowIndex) {
        Map<String, Object> result = new HashMap<>(headers.length);
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i], values[rowIndex][i]);
        }
        return result;
    }

    public Dataframe select(String... columns) {
        List<String> labels = Arrays.asList(headers);
        int[] selected = Arrays.stream(columns).mapToInt(column -> labels.indexOf(column)).toArray();
        Dataframe df = new Dataframe(columns, height());
        for (int rowIndex = 0; rowIndex < values.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                df.setValue(rowIndex, columnIndex, values[rowIndex][selected[columnIndex]]);
            }
        }
        return df;
    }


}
