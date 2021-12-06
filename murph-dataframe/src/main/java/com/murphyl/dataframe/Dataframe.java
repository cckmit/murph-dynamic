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

    public Dataframe(String[] headers, Object[][] values) {
        this.headers = headers;
        this.values = values;
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
