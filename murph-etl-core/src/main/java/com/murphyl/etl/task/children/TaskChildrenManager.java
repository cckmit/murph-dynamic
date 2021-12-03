package com.murphyl.etl.task.children;

import com.murphyl.etl.support.Environments;
import com.murphyl.etl.task.children.extractor.Extractor;
import com.murphyl.etl.task.children.loader.Loader;
import com.murphyl.etl.task.children.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 任务角色 - 管理器
 *
 * @date: 2021/12/2 16:18
 * @author: murph
 */
public final class TaskChildrenManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskChildrenManager.class);

    private static final Map<String, Extractor> REGISTERED_EXTRACTORS;
    private static final Map<String, Transformer> REGISTERED_TRANSFORMERS;
    private static final Map<String, Loader> REGISTERED_LOADERS;

    private static final String DEFAULT_EXTRACTOR = "random";
    private static final String DEFAULT_LOADER = "console";

    static {
        REGISTERED_EXTRACTORS = Environments.loadService("task extractor", Extractor.class);
        REGISTERED_TRANSFORMERS = Environments.loadService("task transformer", Transformer.class);
        REGISTERED_LOADERS = Environments.loadService("task loader", Loader.class);
    }


    public static Extractor getExtractor(String type) {
        return REGISTERED_EXTRACTORS.getOrDefault(type, REGISTERED_EXTRACTORS.get(DEFAULT_EXTRACTOR));
    }

    public static Transformer getTransformer(String type) {
        return null == type ? null : REGISTERED_TRANSFORMERS.get(type);
    }

    public static Loader getLoader(String type) {
        return REGISTERED_LOADERS.getOrDefault(type, REGISTERED_LOADERS.get(DEFAULT_LOADER));
    }

}
