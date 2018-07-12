package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;

/**
 * http environment
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpEnvironment {
    /**
     * port
     */
    private int port;
    /**
     * redirect https or not
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
