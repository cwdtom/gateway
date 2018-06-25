package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;

/**
 * http环境
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpEnvironment {
    /**
     * 端口号
     */
    private int port;
    /**
     * 是否重定向至https
     */
    private boolean redirectHttps;

    HttpEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSONObject.parseObject(config.getChild("http"));
        port = obj.getInteger("port");
        redirectHttps = obj.getBoolean("redirectHttps");
    }

    public int getPort() {
        return port;
    }

    public boolean isRedirectHttps() {
        return redirectHttps;
    }
}
