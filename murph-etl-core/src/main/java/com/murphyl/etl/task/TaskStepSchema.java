package com.murphyl.etl.task;

import java.util.Properties;

/**
 * 任务步骤 - 子节点
 *
 * @date: 2021/11/11 16:18
 * @author: murph
 */
public class TaskStepSchema {


    private String dsl;

    private Properties properties;

    public String getDsl() {
        return dsl;
    }

    public void setDsl(String dsl) {
        this.dsl = dsl;
    }

    public Properties getProperties() {
        if(null == properties) {
            this.properties = new Properties();
        }
        return properties;
    }

    public String getProperty(String key) {
        if(null == properties) {
            this.properties = new Properties();
        }
        return null == key ? null : properties.getProperty(key);
    }

    public void addProperty(String key, String value) {
        if(null == properties) {
            this.properties = new Properties();
        }
        properties.put(key, value);
    }

}
