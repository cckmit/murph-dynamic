package com.murphyl.etl.core.task.schema.impl;

import com.murphyl.etl.core.task.schema.TaskSchema;
import com.murphyl.etl.support.xml.XmlNode;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * -
 *
 * @date: 2021/12/17 15:22
 * @author: murph
 */
public class XmlTaskSchema extends XmlNode implements TaskSchema {

    public XmlTaskSchema(Node node) {
        super(node);
    }

    @Override
    public String dsl() {
        return content();
    }

    @Override
    public String attr(String key) {
        return attribute(key);
    }

    @Override
    public Map<String, String> attrs() {
        return attributes();
    }
}
