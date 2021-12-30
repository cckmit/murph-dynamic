package com.murphyl.saas.modules;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * GraalJS - 支持模组
 *
 * @author: murph
 * @date: 2021/12/26 - 4:55
 */
@Singleton
public class GraalJsEngine {

    private static final Logger logger = LoggerFactory.getLogger(GraalJsEngine.class);

    private static final String SCRIPT_LANGUAGE = JavaScriptLanguage.ID;

    private static final String SCRIPT_MIME_TYPE = JavaScriptLanguage.MODULE_MIME_TYPE;

    private static final HostAccess ACCESS_LOCAL = HostAccess.newBuilder()
            .allowMapAccess(true)
            .allowListAccess(true)
            .allowArrayAccess(true)
            .allowAccessAnnotatedBy(HostAccess.Export.class)
            .allowImplementationsAnnotatedBy(HostAccess.Implementable.class)
            .allowImplementationsAnnotatedBy(FunctionalInterface.class)
            .build();

    private final Context.Builder contextBuilder;

    public GraalJsEngine(Config env) {
        Options settings = ConfigBeanFactory.create(env.getConfig("graaljs.engine"), Options.class);
        Map<String, String> options = new TreeMap<>();
        if (null != settings.getOptions()) {
            for (Map.Entry<String, Object> entry : settings.getOptions().entrySet()) {
                if (null == entry.getValue()) {
                    continue;
                }
                options.put(entry.getKey(), entry.getValue().toString());
            }
        }
        contextBuilder = Context.newBuilder(SCRIPT_LANGUAGE)
                .allowIO(true)
                .options(options)
                .allowExperimentalOptions(true)
                .allowHostAccess(ACCESS_LOCAL)
                .currentWorkingDirectory(Path.of(settings.getCwd()));
    }

    public Value eval(String source) {
        return eval(createSource(source));
    }

    public Value eval(Source source) {
        return contextBuilder.build().eval(source);
    }

    public Source createSource(String script) {
        try {
            return Source.newBuilder(SCRIPT_LANGUAGE, script, null).mimeType(SCRIPT_MIME_TYPE).build();
        } catch (IOException e) {
            throw new IllegalStateException("创建脚本对象出错", e);
        }
    }

    public Value export(Value exports, String key) {
        return (null != exports && exports.hasMembers()) ? exports.getMember(key) : null;
    }


    public class Options {

        private String cwd;

        private Map<String, Object> options;

        public String getCwd() {
            return cwd;
        }

        public void setCwd(String cwd) {
            this.cwd = cwd;
        }

        public Map<String, Object> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Object> options) {
            this.options = options;
        }

    }
}
