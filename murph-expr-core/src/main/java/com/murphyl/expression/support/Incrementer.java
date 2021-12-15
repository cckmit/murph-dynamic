package com.murphyl.expression.support;

import java.util.concurrent.atomic.LongAdder;

/**
 * 序列 - 自增器
 *
 * @date: 2021/12/3 10:20
 * @author: murph
 */
public class Incrementer {

    private final LongAdder adder;

    public Incrementer() {
        this.adder = new LongAdder();
    }

    public long next() {
        adder.increment();
        return adder.longValue();
    }

    public long prev() {
        adder.decrement();
        return adder.longValue();
    }

}
