package com.murphyl.saas.support.web.server;

import com.typesafe.config.Optional;

/**
 * -
 *
 * @author: murph
 * @date: 2021/12/26 - 9:34
 */
public class WebServerOptions {

    private int port;

    @Optional
    private String host;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
