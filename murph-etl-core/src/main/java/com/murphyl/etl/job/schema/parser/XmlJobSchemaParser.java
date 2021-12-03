package com.murphyl.etl.job.schema.parser;

import com.murphyl.dynamic.Group;
import com.murphyl.dynamic.Qualifier;
import com.murphyl.etl.job.schema.JobSchema;
import com.murphyl.etl.task.TaskSchema;
import com.murphyl.etl.task.TaskStepSchema;
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
@Group(JobSchemaParser.class)
@Qualifier({"xml"})
public class XmlJobSchemaParser implements JobSchemaParser {

    private static final String JOB_NAME_XPATH = "/etl-job/@name";
    private static final String JOB_TASKS_XPATH = "/etl-job/task";
    private static final String JOB_PARAMS_XPATH = "/etl-job/params/assign";

    private static final String TASK_EXTRACTOR_XPATH = "extractor";
    private static final String TASK_TRANSFORMERS_XPATH = "transformer";
    private static final String TASK_LOADER_XPATH = "loader";

    private static final String NODE_NAME_XPATH = "@name";
    private static final String NODE_UNIQUE_XPATH = "@id";
    private static final String NODE_VALUE_XPATH = "@value";
    private static final String NODE_PARENTS_XPATH = "@parents";

    @Override
    public JobSchema parse(final String unique) {
        Document document = XmlUtils.parse(unique);
        JobSchema schema = new JobSchema();
        schema.setName(XmlUtils.xpathText(document, JOB_NAME_XPATH));
        schema.setParams(getJobParams(document));
        schema.setTasks(getJobTasks(document));
        return schema;
    }

    public List<TaskSchema> getJobTasks(Document document) {
        NodeList tasks = XmlUtils.xpathList(document, JOB_TASKS_XPATH);
        List<TaskSchema> result = new ArrayList<>(tasks.getLength());
        for (int i = 0; i < tasks.getLength(); i++) {
            result.add(resolveTask(tasks.item(i)));
        }
        return result;
    }

    public Map<String, String> getJobParams(Document document) {
        NodeList params = XmlUtils.xpathList(document, JOB_PARAMS_XPATH);
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
        task.setUnique(XmlUtils.xpathAttr(node, NODE_UNIQUE_XPATH));
        task.setName(XmlUtils.xpathAttr(node, NODE_NAME_XPATH));
        String parents = XmlUtils.xpathText(node, NODE_PARENTS_XPATH);
        if (null != parents) {
            task.setParents(parents.split(","));
        }
        // extractor
        Node extractor = XmlUtils.xpathNode(node, TASK_EXTRACTOR_XPATH);
        task.setExtractor(resolveTaskStepSchema(extractor));
        // transformers
        NodeList transformers = XmlUtils.xpathList(node, TASK_TRANSFORMERS_XPATH);
        if (null != transformers || transformers.getLength() > 0) {
            TaskStepSchema[] resolved = IntStream.range(0, transformers.getLength())
                    // 按照索引转换子模块
                    .mapToObj(index -> resolveTaskStepSchema(transformers.item(index)))
                    // 转换
                    .toArray(TaskStepSchema[]::new);
            task.setTransformers(resolved);
        }
        // loader
        Node loader = XmlUtils.xpathNode(node, TASK_LOADER_XPATH);
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
