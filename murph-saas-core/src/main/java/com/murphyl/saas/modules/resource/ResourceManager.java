package com.murphyl.saas.modules.resource;

import com.murphyl.saas.modules.resource.loaders.FilesystemResourceLoader;
import com.murphyl.saas.support.ResourceLoader;
import com.murphyl.saas.utils.FileUtils;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * 资源 - 管理器
 *
 * @date: 2021/12/30 16:35
 * @author: murph
 */
@Singleton
public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    public static final String RESOURCE_LOADER_KEY = "app.resource.loader";

    public static final String[] RESOURCE_LOADER_TYPES = {"filesystem"};

    private String cwd;

    @Inject
    public ResourceManager(Config env) {
        Config config = env.getConfig("graaljs.engine");
        this.cwd = config.getString("cwd");
        logger.info("正在初始化资源管理器，工作目录：{}", config.withoutPath("options"));
    }


    public Collection<File> getScripts() {
        return FileUtils.list(Paths.get(cwd, "javascript").toFile(), true);
    }

    public enum Loaders {
        /**
         * 从文件系统加载资源
         */
        filesystem(FilesystemResourceLoader.class),
        ;

        private Class loaderClass;

        <T extends ResourceLoader> Loaders(Class<T> loaderClass) {
            this.loaderClass = loaderClass;
        }

        public Class getLoaderClass() {
            return loaderClass;
        }
    }

}
