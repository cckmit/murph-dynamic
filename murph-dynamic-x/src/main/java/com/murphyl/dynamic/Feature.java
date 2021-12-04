package com.murphyl.dynamic;

/**
 * 动态 - 插件
 * author: murph
 * 2021/12/3 - 21:49
 */
public interface Feature {

    /**
     * 别名
     *
     * @return
     */
    default String[] alias() {
        if (!getClass().isAnnotationPresent(Qualifier.class)) {
            return null;
        }
        return getClass().getDeclaredAnnotation(Qualifier.class).value();
    }


}
