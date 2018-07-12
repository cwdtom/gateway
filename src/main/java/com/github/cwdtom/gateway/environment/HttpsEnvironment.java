package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;

/**
 * https environment
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpsEnvironment {
    /**
     * enable
     */
    private boolean enable;
    /**
     * port
     */
    private int port;
    /**
     * certificate password
     */
    private String keyPwd;
    /**
     * certificate file path
     */
    private String keyPath;

    HttpsEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSONObject.parseObject(config.getChild("https"));
        if (obj == null) {
            enable = false;
        } else {
            enable = obj.getBoolean("enable");
            port = obj.getInteger("port");
            keyPwd = obj.getString("keyPwd");
            keyPath = obj.getString("keyPath");
        }
    }

    public int getPort() {
        return port;
    }

    public String getKeyPwd() {
        return keyPwd;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public boolean isEnable() {
        return enable;
    }
}
