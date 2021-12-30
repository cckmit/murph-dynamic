package com.murphyl.saas.support.resource.graaljs;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JavaScript - 脚本支持
 *
 * @date: 2021/12/23 17:20
 * @author: murph
 */
public final class EcmaScriptSupport {

    private static final Logger logger = LoggerFactory.getLogger(EcmaScriptSupport.class);

    private static final Context.Builder CONTEXT_BUILDER;

    private static final String SCRIPT_LANGUAGE;

    private static final String SCRIPT_MIME_TYPE;

    private static final HostAccess ACCESS_LOCAL = HostAccess.newBuilder()
            .allowMapAccess(true)
            .allowListAccess(true)
            .allowArrayAccess(true)
            .allowAccessAnnotatedBy(HostAccess.Export.class)
            .allowImplementationsAnnotatedBy(HostAccess.Implementable.class)
            .allowImplementationsAnnotatedBy(FunctionalInterface.class)
            .build();

    static {
        // options: js.atomics, js.v8-compat, js.esm-eval-returns-exports
        Config settings = ConfigFactory.load().getConfig("script.engine");
        SCRIPT_LANGUAGE = settings.getString("current");
        Config config = settings.getConfig("options").getConfig(SCRIPT_LANGUAGE);
        Set<Map.Entry<String, ConfigValue>> entries = config.entrySet();
        Map<String, String> options = new HashMap<>(entries.size());
        for (Map.Entry<String, ConfigValue> entry : entries) {
            if (null != entry.getValue()) {
                options.put(entry.getKey(), entry.getValue().render());
            }
        }
        CONTEXT_BUILDER = Context.newBuilder(SCRIPT_LANGUAGE)
                .allowIO(true)
                .options(options)
                .allowExperimentalOptions(true)
                .allowHostAccess(ACCESS_LOCAL)
                .allowHostClassLookup(identifier -> {
                    logger.info("JavaScript 尝试交接 Java 对象：{}", identifier);
                    return true;
                })
                .currentWorkingDirectory(Path.of(settings.getString("cwd")));

        if (JavaScriptLanguage.ID.equalsIgnoreCase(SCRIPT_LANGUAGE)) {
            SCRIPT_MIME_TYPE = JavaScriptLanguage.MODULE_MIME_TYPE;
        } else {
            SCRIPT_MIME_TYPE = null;
        }
    }

    public static Value eval(String source) {
        return eval(createSource(source));
    }

    public static Value eval(Source source) {
        return CONTEXT_BUILDER.build().eval(source);
    }

    public static Source createSource(String script) {
        try {
            return Source.newBuilder(SCRIPT_LANGUAGE, script, null).mimeType(SCRIPT_MIME_TYPE).build();
        } catch (IOException e) {
            throw new IllegalStateException("创建脚本对象出错", e);
        }
    }

    public static Value export(Value exports, String key) {
        return (null != exports && exports.hasMembers()) ? exports.getMember(key) : null;
    }
}
