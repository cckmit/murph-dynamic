package com.murphyl.saas.support.expression;

import com.murphyl.saas.support.Environments;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSContextOptions;
import com.typesafe.config.Config;
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
public final class JavaScriptSupport {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptSupport.class);

    private static final Context.Builder CONTEXT_BUILDER;

    private static final String SCRIPT_LANGUAGE;

    private static final String SCRIPT_MIME_TYPE;

    static {
        // options: js.atomics, js.v8-compat, js.esm-eval-returns-exports
        SCRIPT_LANGUAGE = Environments.getString("app.script.engine");
        Config config = Environments.getConfig("script.engine." + SCRIPT_LANGUAGE);
        Set<Map.Entry<String, ConfigValue>> entries = config.entrySet();
        Map<String, String> options = new HashMap<>(entries.size());
        for (Map.Entry<String, ConfigValue> entry : entries) {
            if (null != entry.getValue()) {
                options.put(SCRIPT_LANGUAGE + '.' + entry.getKey(), entry.getValue().unwrapped().toString());
            }
        }
        CONTEXT_BUILDER = Context.newBuilder(SCRIPT_LANGUAGE)
                .allowIO(true)
                .options(options)
                .allowExperimentalOptions(true)
                .allowHostAccess(HostAccess.ALL)
                .currentWorkingDirectory(Path.of(Environments.getString("user.dir"), "murph-saas-core/src/main/resources/META-INF/javascript/"));

        if (JavaScriptLanguage.ID.equalsIgnoreCase(SCRIPT_LANGUAGE)) {
            CONTEXT_BUILDER.option(JSContextOptions.ESM_EVAL_RETURNS_EXPORTS_NAME, "true");
            SCRIPT_MIME_TYPE = JavaScriptLanguage.MODULE_MIME_TYPE;
        } else {
            SCRIPT_MIME_TYPE = null;
        }
    }


    public static Value eval(Source source, Map<String, ?> params) {
        Context context = CONTEXT_BUILDER.build();
        if (null != params && !params.isEmpty()) {
            Value bindings = context.getBindings(SCRIPT_LANGUAGE);
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                if (null == entry.getValue()) {
                    continue;
                }
                bindings.putMember(entry.getKey(), entry.getValue());
            }
        }
        try {
            return context.eval(source);
        } finally {
            // context.close();
        }
    }

    public static Source createSource(String script) {
        try {
            return Source.newBuilder(SCRIPT_LANGUAGE, script, null).mimeType(SCRIPT_MIME_TYPE).build();
        } catch (IOException e) {
            throw new IllegalStateException("创建脚本对象出错", e);
        }
    }

    public static boolean isValid(Value exports) {
        return null != exports && exports.hasMembers();
    }
}
