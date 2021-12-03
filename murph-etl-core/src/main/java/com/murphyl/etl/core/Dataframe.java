package com.murphyl.etl.core;

/**
 * 数据帧
 *
 * @date: 2021/12/2 18:54
 * @author: murph
 */
public class Dataframe {

    private String[] headers;
    private final Object[][] values;

    public Dataframe(int width, int height) {
        this.values = new Object[height][width];
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String[] getHeaders() {
        return headers;
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
        return this.values[rowIndex];
    }
}
