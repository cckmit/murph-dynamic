package com.murphyl.saas.support.web.schema.loader.impl;

import com.murphyl.saas.support.expression.graaljs.JavaScriptSupport;
import com.murphyl.saas.support.web.schema.RestRoute;
import com.murphyl.saas.support.web.schema.manager.RestProfileLoader;
import com.murphyl.saas.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FilesystemRouteProfileLoader implements RestProfileLoader {

    private static final Logger logger = LoggerFactory.getLogger(FilesystemRouteProfileLoader.class);

    private final Path root;
    private final Map<String, Map<String, String>> routes;

    public FilesystemRouteProfileLoader(Map<String, Object> options) {
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
            File script = scripts.get(Path.of(filename).normalize().toString());
            Value exports = JavaScriptSupport.eval(FileUtils.read(script));
            Map<String, String> table = route.getValue();
            if (null == table) {
                throw new IllegalStateException("Rest 模块[" + filename + "]路由表配置不能为空");
            }
            for (Map.Entry<String, String> rule : table.entrySet()) {
                String module = rule.getKey();
                if (!exports.hasMember(module)) {
                    logger.warn("脚本文件（{}）未导出指定函数：{}", filename, module);
                    continue;
                }
                if (!exports.getMember(module).canExecute()) {
                    logger.warn("脚本文件（{}）导出的对象（{}）无法被执行", filename, module);
                    continue;
                }
                logger.info("正在注册路由规则：file=[{}], endpoint=[{}], binding=[{}]", filename, module, rule.getValue());
                result.add(convert(filename, script, module, rule.getValue(), exports.getMember(module)));
            }
        }
        return result;
    }

    private RestRoute convert(String filename, File script, String endpoint, String head, Value function) {
        RestRoute result = new RestRoute();
        result.setFile(script.getAbsolutePath());
        result.setModified(script.lastModified());
        result.setNamespace(namespace("file", filename, endpoint));
        result.setFunction(function);
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
