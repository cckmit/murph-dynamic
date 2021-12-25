package com.murphyl.saas.support.web.schema;

/**
 * Rest - 请求
 *
 * @date: 2021/12/24 18:45
 * @author: murph
 */
public class RestRoute<T> {

    private String namespace;

    private String file;

    private Long modified;

    private String method;

    private String path;

    private T function;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Long getModified() {
        return modified;
    }

    public void setModified(Long modified) {
        this.modified = modified;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public T getFunction() {
        return function;
    }

    public void setFunction(T function) {
        this.function = function;
    }
}
