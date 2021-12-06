package com.murphyl.dataframe.support;

import com.murphyl.dataframe.Dataframe;

/**
 * -
 *
 * @date: 2021/12/6 19:39
 * @author: murph
 */
public class AsciiTable {

    private final String[] headers;
    private final Object[][] values;

    boolean showRowIndex = true;

    private String nullValue = "";
    private String columnSeparator = "\t";

    private String headerPrefix = "[";
    private String headerSuffix = "]";

    public AsciiTable(Dataframe dataframe) {
        headers = dataframe.getHeaders();
        values = dataframe.getValues();
    }

    public void setShowRowIndex(boolean showRowIndex) {
        this.showRowIndex = showRowIndex;
    }

    public AsciiTable setNullValue(String nullValue) {
        this.nullValue = nullValue;
        return this;
    }

    public AsciiTable setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
        return this;
    }

    public AsciiTable setHeaderPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
        return this;
    }

    public AsciiTable setHeaderSuffix(String headerSuffix) {
        this.headerSuffix = headerSuffix;
        return this;
    }

    public String render() {
        return render(0, values.length - 1);
    }

    public synchronized String render(int from, int to) {
        if (from < 1 && to < 1) {
            throw new IllegalStateException(String.format("[from(%d)] and [to(%d)] must great than 0", from, to));
        }
        if(from > to) {
            throw new IllegalStateException(String.format("[from(%d)] must less [to(%d)]", from, to));
        }
        StringBuilder builder = new StringBuilder();
        if (null != headers && headers.length > 0) {
            builder.append(headerPrefix);
            if(showRowIndex) {
                builder.append("index").append(columnSeparator);
            }
            for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
                if (columnIndex > 0) {
                    builder.append(columnSeparator);
                }
                builder.append(null == headers[columnIndex] ? nullValue : headers[columnIndex]);
            }
            builder.append(headerSuffix).append(System.lineSeparator());
        }
        if (null != values && values.length > 0) {
            for (int rowIndex = from; rowIndex < to && rowIndex < values.length; rowIndex++) {
                if (rowIndex > from) {
                    builder.append(System.lineSeparator());
                }
                if(showRowIndex) {
                    builder.append(rowIndex).append(columnSeparator);
                }
                for (int columnIndex = 0; columnIndex < values[rowIndex].length; columnIndex++) {
                    if (columnIndex > 0) {
                        builder.append(columnSeparator);
                    }
                    builder.append(null == values[rowIndex][columnIndex] ? nullValue : values[rowIndex][columnIndex]);
                }
            }
        }
        return builder.toString();
    }

    public String renderBatch(int batchNo, int batchSize) {
        if (batchNo < 1 && batchSize < 1) {
            throw new IllegalStateException(String.format("[batchNo(%d)] and [batchSize(%d)] must great than 0", batchNo, batchSize));
        }
        return render(batchNo * batchSize, (batchNo + 1) * batchSize);
    }
}
