package com.murphyl.etl.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * XML 工具类
 *
 * @date: 2021/11/11 11:58
 * @author: murph
 */
public final class XmlUtils {

    private static final DocumentBuilder DOC_BUILDER;
    private static final XPath XPATH_EVALUATOR;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DOC_BUILDER = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("创建 XML 解析器出错", e);
        }
        XPATH_EVALUATOR = XPathFactory.newInstance().newXPath();
    }

    public synchronized static Document parse(String path) {
        try(FileInputStream reader = new FileInputStream(path)) {
            Document document = DOC_BUILDER.parse(reader);
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception e) {
            throw new IllegalStateException("解析 XML 文档出错", e);
        }
    }

    public static NodeList xpathList(Node document, String path) {
        try {
            return (NodeList) XPATH_EVALUATOR.evaluate(path, document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("使用 XPath 解析 XML 节点列表出错", e);
        }
    }

    public static List<Node> xpathList(Node document) {
        try {
            NodeList items = (NodeList) XPATH_EVALUATOR.evaluate("*", document, XPathConstants.NODESET);
            List<Node> result = new ArrayList<>(items.getLength());
            for (int i = 0; i < items.getLength(); i++) {
                result.add(items.item(i));
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("使用 XPath 解析 XML 节点列表出错", e);
        }
    }

    public static Node xpathNode(Node document, String path) {
        try {
            return (Node) XPATH_EVALUATOR.evaluate(path, document, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("使用 XPath 解析 XML 单个节点出错", e);
        }
    }

    public static String xpathAttr(Node document, String path) {
        try {
            return (String) XPATH_EVALUATOR.evaluate(path, document, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("使用 XPath 解析 XML 节点属性出错", e);
        }
    }

    public static String xpathText(Node document, String path) {
        try {
            Node node = xpathNode(document, path);
            return null == node ? null : node.getTextContent();
        } catch (Exception e) {
            throw new IllegalStateException("使用 XPath 解析 XML 节点属性出错", e);
        }
    }

}
