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
     * 单例
     */
    private static HttpsEnvironment instance;
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

    /**
     * 获取https环境对象
     *
     * @return https环境对象
     */
    public static HttpsEnvironment get() {
        return instance;
    }

    static {
        JSONObject obj = ConfigEnvironment.getChild("https");
        HttpsEnvironment env = new HttpsEnvironment();
        env.enable = obj.getBoolean("enable");
        env.port = obj.getInteger("port");
        env.keyPwd = obj.getString("keyPwd");
        env.keyPath = obj.getString("keyPath");
        HttpsEnvironment.instance = env;
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
