package com.murphyl.etl.utils;

import com.murphyl.etl.support.Qualifier;

/**
 * 反射 - 工具类
 *
 * @date: 2021/12/1 11:53
 * @author: murph
 */
public final class ReflectUtils {

    public static <T> String[] getAlias(T object) {
        if (null == object || !object.getClass().isAnnotationPresent(Qualifier.class)) {
            return null;
        }
        Qualifier qualifier = object.getClass().getDeclaredAnnotation(Qualifier.class);
        if (null == qualifier) {
            return null;
        }
        return qualifier.value();
    }

}
