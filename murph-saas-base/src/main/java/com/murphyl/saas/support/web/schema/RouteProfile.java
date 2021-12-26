package com.murphyl.saas.support.web.schema;

import java.util.Map;

/**
 * Rest - 模块配置
 *
 * @date: 2021/12/24 15:53
 * @author: murph
 */
public class RouteProfile {

    private String loader;

    private Map<String, Object> loaders;

    private Map<String, Object> options;

    public String getLoader() {
        return loader;
    }

    public void setLoader(String loader) {
        this.loader = loader;
    }

    public Map<String, Object> getLoaders() {
        return loaders;
    }

    public void setLoaders(Map<String, Object> loaders) {
        this.loaders = loaders;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
