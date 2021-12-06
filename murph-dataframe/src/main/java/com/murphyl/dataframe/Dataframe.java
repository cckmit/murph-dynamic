package com.murphyl.dataframe;

import java.util.Arrays;
import java.util.List;

/**
 * 数据帧
 *
 * @date: 2021/12/2 18:54
 * @author: murph
 */
public class Dataframe {

    private String[] headers;
    private final Object[][] values;

    public Dataframe(String[] headers, int height) {
        this.headers = headers;
        this.values = new Object[height][headers.length];
    }

    public Dataframe(String[] headers, Object[][] payload) {
        this.headers = headers;
        this.values = payload;
    }

    public Dataframe(String[] headers, List<Object[]> payload) {
        this.headers = headers;
        this.values = new Object[payload.size()][headers.length];
        for (int rowIndex = 0; rowIndex < payload.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
                setValue(rowIndex, columnIndex, payload.get(rowIndex)[columnIndex]);
            }
        }
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

    public Object[] row(int rowIndex) {
        return values[rowIndex];
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
