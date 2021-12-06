package com.murphyl.etl.core.job.schema.parser;

import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.support.Consts;
import com.murphyl.etl.core.job.schema.JobSchema;
import com.murphyl.etl.core.task.TaskSchema;
import com.murphyl.etl.core.task.TaskStepSchema;
import com.murphyl.etl.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * XML 配置工厂类
 *
 * @date: 2021/11/11 13:16
 * @author: murph
 */
@Qualifier({"xml"})
public class XmlJobSchemaParser implements JobSchemaParser {

    private static final String NODE_NAME_XPATH = "@name";
    private static final String NODE_VALUE_XPATH = "@value";

    private static final String NODE_PARENTS_XPATH = "@parents";

    public static final String XPATH_OF_ETL_JOB_NAME = String.format("%s/%s", Consts.XPATH_OF_ETL_JOB, NODE_NAME_XPATH);

    @Override
    public JobSchema parse(final String unique) {
        Document document = XmlUtils.parse(unique);
        JobSchema schema = new JobSchema();
        schema.setName(XmlUtils.xpathText(document, XPATH_OF_ETL_JOB_NAME));
        schema.setParams(getJobParams(document));
        schema.setTasks(getJobTasks(document));
        return schema;
    }

    public List<TaskSchema> getJobTasks(Document document) {
        NodeList tasks = XmlUtils.xpathList(document, Consts.XPATH_OF_ETL_JOB_TASKS);
        List<TaskSchema> result = new ArrayList<>(tasks.getLength());
        for (int i = 0; i < tasks.getLength(); i++) {
            result.add(resolveTask(tasks.item(i)));
        }
        return result;
    }

    public Map<String, String> getJobParams(Document document) {
        NodeList params = XmlUtils.xpathList(document, Consts.XPATH_OF_ETL_JOB_PARAM);
        Map<String, String> result = new HashMap<>(params.getLength());
        Node param;
        for (int i = 0; i < params.getLength(); i++) {
            param = params.item(i);
            result.put(XmlUtils.xpathAttr(param, NODE_NAME_XPATH), XmlUtils.xpathAttr(param, NODE_VALUE_XPATH));
        }
        return result;
    }


    private TaskSchema resolveTask(Node node) {
        TaskSchema task = new TaskSchema();
        task.setName(XmlUtils.xpathAttr(node, NODE_NAME_XPATH));
        String parents = XmlUtils.xpathText(node, NODE_PARENTS_XPATH);
        if (null != parents) {
            task.setParents(parents.split(","));
        }
        // extractor
        Node extractor = XmlUtils.xpathNode(node, Consts.TASK_ROLE_EXTRACTOR);
        task.setExtractor(resolveTaskStepSchema(extractor));
        // transformers
        NodeList transformers = XmlUtils.xpathList(node, Consts.TASK_ROLE_TRANSFORMER);
        if (null != transformers || transformers.getLength() > 0) {
            TaskStepSchema[] resolved = IntStream.range(0, transformers.getLength())
                    // 按照索引转换子模块
                    .mapToObj(index -> resolveTaskStepSchema(transformers.item(index)))
                    // 转换
                    .toArray(TaskStepSchema[]::new);
            task.setTransformers(resolved);
        }
        // loader
        Node loader = XmlUtils.xpathNode(node, Consts.TASK_ROLE_LOADER);
        task.setLoader(resolveTaskStepSchema(loader));
        return task;
    }

    public TaskStepSchema resolveTaskStepSchema(Node node) {
        TaskStepSchema schema = new TaskStepSchema();
        schema.setDsl(node.getTextContent());
        NamedNodeMap attrs = node.getAttributes();
        Node attr;
        for (int i = 0; i < attrs.getLength(); i++) {
            attr = attrs.item(i);
            schema.addProperty(attr.getNodeName(), attr.getTextContent());
        }
        return schema;
    }

}
