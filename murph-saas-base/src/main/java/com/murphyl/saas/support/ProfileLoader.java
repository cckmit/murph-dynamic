package com.murphyl.saas.support;

import java.util.List;

/**
 * 配置 - 加载器
 *
 * @date: 2021/12/30 11:23
 * @author: murph
 */
public interface ProfileLoader {

    /**
     * 加载配置
     *
     * @return
     * @throws Exception
     */
    <T> List<T> load();

}
