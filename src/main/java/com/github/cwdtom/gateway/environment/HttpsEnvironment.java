package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * https环境
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpsEnvironment {
    /**
     * 单例
     */
    private static HttpsEnvironment instance;
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
}
