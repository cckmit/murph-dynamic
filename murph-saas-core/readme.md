```sh
# java.lang.reflect.InaccessibleObjectException: Unable to make protected final java.lang.Class java.lang.ClassLoader.defineClass 报错的情况处理
java --add-opens java.base/java.lang=ALL-UNNAMED -Dconfig.trace=loads -Dconfig.file=$MODULE_DIR$/src/main/resources/test.conf
```

### TODO

- [ ] UdfSourceAssembly
- [ ] 脚本化 HTTP 请求 - [Apache Commons JEXL - Syntax](https://commons.apache.org/proper/commons-jexl/reference/syntax.html)
- [ ] [配置文件](https://github.com/lightbend/config/blob/main/HOCON.md)
- [ ] JXTL 模板 - [Provides a framework for evaluating JEXL expressions](https://commons.apache.org/proper/commons-jexl/apidocs/org/apache/commons/jexl3/package-summary.html#usage)
- [ ] Vert.x 启动器 - [Vert.x Command Line Interface API](https://vertx.io/docs/vertx-core/java/#_vert_x_command_line_interface_api)
- [ ] [List of In-Memory Databases](https://www.baeldung.com/java-in-memory-databases)
