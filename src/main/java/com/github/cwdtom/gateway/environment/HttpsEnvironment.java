package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;

/**
 * https环境
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpsEnvironment {
    /**
     * 是否开启
     */
    private boolean enable;
    /**
     * 端口号
     */
    private int port;
    /**
     * 证书密码
     */
    private String keyPwd;
    /**
     * 证书文件路径
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
