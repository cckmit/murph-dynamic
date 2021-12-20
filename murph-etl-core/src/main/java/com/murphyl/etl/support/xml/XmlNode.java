package com.murphyl.etl.support.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * -
 *
 * @date: 2021/12/17 15:20
 * @author: murph
 */
public class XmlNode {

    private Node node;

    public XmlNode(Node node) {
        this.node = node;
    }

    protected String content() {
        String content = node.getTextContent();
        return Arrays.stream(content.split("\n"))
                .filter(line -> line.trim().length() > 0)
                .map(line -> line.trim())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    protected String attribute(String key) {
        return attributes().get(key);
    }

    protected Map<String, String> attributes() {
        NamedNodeMap attributes = node.getAttributes();
        Map<String, String> result = new HashMap<>(attributes.getLength());
        for (int i = 0; i < attributes.getLength(); i++) {
            result.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
        }
        return result;
    }

}
