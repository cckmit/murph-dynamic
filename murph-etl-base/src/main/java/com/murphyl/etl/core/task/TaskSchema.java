package com.murphyl.etl.core.task;

import java.util.Map;

/**
 * 任务步骤 - 实例
 *
 * @date: 2021/11/11 14:25
 * @author: murph
 */
public class TaskSchema {

    private String name;
    private String[] parents;
    private Map<String, String> params;

    private TaskStepSchema extractor;
    private TaskStepSchema[] transformers;
    private TaskStepSchema loader;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getParents() {
        return parents;
    }

    public void setParents(String[] parents) {
        this.parents = parents;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public TaskStepSchema getExtractor() {
        return extractor;
    }

    public void setExtractor(TaskStepSchema extractor) {
        this.extractor = extractor;
    }

    public TaskStepSchema getLoader() {
        return loader;
    }

    public void setLoader(TaskStepSchema loader) {
        this.loader = loader;
    }

    public TaskStepSchema[] getTransformers() {
        return transformers;
    }

    public void setTransformers(TaskStepSchema[] transformers) {
        this.transformers = transformers;
    }
}
