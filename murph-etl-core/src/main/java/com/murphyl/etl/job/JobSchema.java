package com.murphyl.etl.job;

import com.murphyl.etl.task.TaskSchema;

import java.util.List;
import java.util.Map;

/**
 * 任务 - 配置
 *
 * @date: 2021/12/1 12:51
 * @author: murph
 */
public class JobSchema {

    private String name;

    private Map<String, String> params;

    private List<TaskSchema> tasks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public List<TaskSchema> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskSchema> tasks) {
        this.tasks = tasks;
    }
}
