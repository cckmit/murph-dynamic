package com.murphyl.saas.modules;

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

}
