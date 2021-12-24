package com.murphyl.saas.support.rest.schema;

import io.vertx.core.http.HttpMethod;
import org.graalvm.polyglot.Value;

/**
 * Rest - 请求
 *
 * @date: 2021/12/24 18:45
 * @author: murph
 */
public class RestRoute {

    private String namespace;

    private HttpMethod method;

    private String path;

    private Value function;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Value getFunction() {
        return function;
    }

    public void setFunction(Value function) {
        this.function = function;
    }
}
