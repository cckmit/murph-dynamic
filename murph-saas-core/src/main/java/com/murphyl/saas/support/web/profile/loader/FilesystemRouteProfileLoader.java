package com.murphyl.saas.support.web.profile.loader;

import com.murphyl.saas.support.web.profile.RestRoute;
import com.murphyl.saas.support.web.profile.RouteProfile;
import com.murphyl.saas.utils.FileUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Filesystem - Rest 配置加载
 *
 * @date: 2021/12/24 15:37
 * @author: murph
 */
public class FilesystemRouteProfileLoader extends AbstractRouteProfileLoader {

    private static final Logger logger = LoggerFactory.getLogger(FilesystemRouteProfileLoader.class);

    private final Path root;
    private final Map<String, Map<String, String>> routes;

    @Inject
    public FilesystemRouteProfileLoader(Config configModule) {
        RouteProfile profile = ConfigBeanFactory.create(configModule.getConfig("rest.profile"), RouteProfile.class);
        Map<String, Object> options = profile.getOptions();
        Object path = options.get("path");
        Objects.requireNonNull(path, "从文件系统加载 Rest 配置时必须通过配置项[rest.profile.options.path]指定脚本路径");
        logger.info("从文件系统加载 Rest 配置：{}", path);
        this.root = Path.of((String) path);
        if (Files.notExists(root)) {
            throw new IllegalStateException("指定的 Rest 配置路径${rest.profile.options.path}在文件系统上不存在");
        }
        this.routes = (Map<String, Map<String, String>>) options.get("routes");
        Objects.requireNonNull(routes, "从文件系统加载 Rest 配置时必须通过配置项[rest.profile.options.routes]指定路由信息");
    }

    @Override
    public String name() {
        return "filesystem";
    }

    @Override
    public List<RestRoute> load() {
        Collection<File> files = FileUtils.list(root.toFile(), true);
        Map<String, File> scripts = new HashMap<>(files.size());
        for (File file : files) {
            scripts.put(root.relativize(file.toPath()).toString(), file);
        }
        List<RestRoute> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> route : routes.entrySet()) {
            String filename = route.getKey();
            File location = scripts.get(Path.of(filename).normalize().toString());
            Map<String, String> table = route.getValue();
            if (null == table) {
                throw new IllegalStateException("Rest 模块[" + filename + "]路由表配置不能为空");
            }
            for (Map.Entry<String, String> rule : table.entrySet()) {
                result.add(convert(filename, location, rule.getKey(), rule.getValue()));
            }
        }
        return result;
    }

    private RestRoute convert(String filename, File file, String module, String head) {
        logger.info("Loading route rule: file=[{}], endpoint=[{}], binding=[{}]", filename, module, head);
        RestRoute result = new RestRoute();
        result.setFilepath(file.getAbsolutePath());
        result.setEndpoint(module);
        result.setModified(file.lastModified());
        result.setNamespace(namespace("file", filename, module));
        String[] parts = StringUtils.split(head, " ", 2);
        if (parts.length == 1) {
            result.setPath(StringUtils.trim(parts[0]));
        } else {
            try {
                result.setMethod(parts[0]);
            } catch (Exception e) {
                throw new IllegalStateException("路由配置（" + head + "）存在问题，指定的请求方法不存在：" + parts[0]);
            }
            result.setPath(StringUtils.trim(parts[1]));
        }
        return result;
    }

}
