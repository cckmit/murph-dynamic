> DAMAO - Dataway - Rest - UDF Source

```java
package com.zhaogang.common.udfs;

import com.alibaba.fastjson.JSON;
import com.zhaogang.scm.common.log.Log;
import com.zhaogang.scm.common.util.LogUtil;
import net.hasor.dataql.UdfSourceAssembly;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Dataway - UDF Source
 *
 * @date: 2021/12/27 11:53
 * @author: hao.luo <hao.luo@china.zhaogang.com>
 */
public class ExtraUdfSource implements UdfSourceAssembly {

    private final static Log log = LogUtil.getLogger();

    /**
     * 发布 HTTP 请求
     *
     * @param params
     * @return
     * @throws IOException
     */
    public Map<String, Object> sendHttpRequest(Map<String, Object> params) {
        Map<String, Object> result = new TreeMap<>();
        try {
            log.info("执行 HTTP 请求", JSON.toJSONString(params));
            Object url = params.get("url");
            Validate.isTrue(null == url || url instanceof String, "请求 URL 不能为空且必须是字符串");
            Object method = params.getOrDefault("method", "GET");
            Validate.isInstanceOf(String.class, method, "请求方法必须是字符串");
            RequestBuilder requestBuilder = RequestBuilder.create((String) method).setUri((String) url);
            // 处理请求头
            Object headers = params.get("headers");
            requestBuilder.addHeader("User-Agent", "Dataway/" + ExtraUdfSource.class.getCanonicalName());
            if (null != headers) {
                if (headers instanceof Map) {
                    for (Map.Entry<String, String> entry : ((Map<String, String>) headers).entrySet()) {
                        requestBuilder.addHeader(entry.getKey(), entry.getValue());
                    }
                } else {
                    log.warn("HTTP 请求头必须是 Map 类型，忽略配置的请求头", JSON.toJSONString(params));
                }
            }
            // 处理参数
            Object parameters = params.get("params");
            requestBuilder.addParameter("ts", String.valueOf(System.currentTimeMillis() / 1000));
            if (null != parameters) {
                if (parameters instanceof Map) {
                    for (Map.Entry<String, String> entry : ((Map<String, String>) parameters).entrySet()) {
                        requestBuilder.addParameter(entry.getKey(), entry.getValue());
                    }
                } else {
                    log.warn("HTTP 请求参数必须是 Map 类型，忽略配置的请求参数", JSON.toJSONString(parameters));
                }
            }
            // 处理请求体
            Object body = params.get("body");
            if (null != body) {
                if (body instanceof String) {
                    requestBuilder.setEntity(new StringEntity((String) body));
                } else {
                    requestBuilder.setEntity(new StringEntity(JSON.toJSONString(body)));
                }
            }
            Object timeout = params.get("timeout");
            log.info("HTTP 请求超时（" + ((timeout == null) ? null : timeout.getClass()) + "）配置：", timeout);
            RequestConfig.Builder configBuilder = RequestConfig.custom();
            if (null == timeout || !(timeout instanceof Number)) {
                configBuilder.setConnectTimeout(3 * 1000)
                        .setConnectionRequestTimeout(6 * 1000)
                        .setSocketTimeout(9 * 1000);
            } else {
                int second = ((Number) timeout).intValue();
                configBuilder.setConnectTimeout(second * 1000)
                        .setConnectionRequestTimeout(second * 2 * 1000)
                        .setSocketTimeout(second * 3 * 1000);
            }
            requestBuilder.setConfig(configBuilder.build());
            RequestConfig requestConfig = requestBuilder.getConfig();
            log.info("请求配置：ConnectTimeout", requestConfig.getConnectTimeout());
            log.info("请求配置：ConnectionRequestTimeout", requestConfig.getConnectionRequestTimeout());
            log.info("请求配置：SocketTimeout", requestConfig.getSocketTimeout());
            // 处理请求
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                CloseableHttpResponse response = client.execute(requestBuilder.build());
                log.info("请求执行完成", response.getStatusLine());
                result.put("status", true);
                result.put("content", IOUtils.toString(response.getEntity().getContent()));
                result.put("statusLine", response.getStatusLine());
                response.close();
            }
        } catch (Exception e) {
            log.error(e, "发送 HTTP 请求失败");
            result.put("status", false);
            result.put("message", e.getMessage());
        } finally {
            log.info("发送 HTTP 请求完成");
        }
        return result;
    }


}
```
