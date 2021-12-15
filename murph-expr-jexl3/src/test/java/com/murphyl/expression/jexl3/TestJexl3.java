package com.murphyl.expression.jexl3;

import org.apache.commons.jexl3.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * -
 *
 * @date: 2021/12/15 15:26
 * @author: murph
 */
public class TestJexl3 {

    @Test
    public void test() {
        JexlEngine jexl = new JexlBuilder().create();
        JxltEngine jxltEngine = jexl.createJxltEngine(true);
        JxltEngine.Expression expression = jxltEngine.createExpression("#{instance.uuid()};hello a = ${a}, uuid = ${instance.uuid()}");
        MapContext context = new MapContext();
        context.set("a", 1);
        context.set("instance", new TestJexl3());
        context.set("static", TestJexl3.class);
        System.out.println(expression.evaluate(context));
        System.out.println(expression.getVariables());
    }

    public UUID uuid() {
        return UUID.randomUUID();
    }

    public static long ts() {
        return System.currentTimeMillis();
    }

}
