package com.murphyl.expression.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.UUID;

/**
 * 表达式
 *
 * @date: 2021/12/2 20:42
 * @author: murph
 */
public final class PreparedExpressions {

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static TemporalAccessor now() {
        return LocalDateTime.now();
    }

    public static long timestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 生成 UUID
     *
     * @return
     */
    public static UUID uuid() {
        return UUID.randomUUID();
    }

    public static Incrementer seq() {
        return new Incrementer();
    }

    /**
     * 计算日期 - 日
     *
     * @param count
     * @return
     */
    public static Temporal date_add(Temporal ts, long count) {
        return ts.plus(count, ChronoUnit.DAYS);
    }

    /**
     * 指定单位运算时间
     *
     * @param ts
     * @param count
     * @param unit
     * @return
     */
    public static Temporal date_add(Temporal ts, long count, String unit) {
        return ts.plus(count, ChronoUnit.valueOf(unit));
    }

    /**
     * 计算日期 - 日
     *
     * @param count
     * @return
     */
    public static Temporal date_minus(Temporal ts, long count) {
        return ts.minus(count, ChronoUnit.DAYS);
    }

    /**
     * 指定单位运算时间
     *
     * @param ts
     * @param count
     * @param unit
     * @return
     */
    public static Temporal date_minus(Temporal ts, long count, String unit) {
        return ts.minus(count, ChronoUnit.valueOf(unit));
    }

    /**
     * 格式化时间
     *
     * @param value
     * @param format
     * @param <P>
     * @return
     */
    public static <P> String format(P value, String format) {
        if (value instanceof Temporal) {
            return DateTimeFormatter.ofPattern(format).format((Temporal) value);
        }
        return UUID.randomUUID().toString();
    }

}
