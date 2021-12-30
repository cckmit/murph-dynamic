package com.murphyl.saas.support;

import java.util.List;

/**
 * 资源 - 加载器
 *
 * @date: 2021/12/30 11:23
 * @author: murph
 */
public interface ResourceLoader {

    /**
     * 加载配置
     *
     * @return 资源列表
     */
    <T> List<T> load();

}
