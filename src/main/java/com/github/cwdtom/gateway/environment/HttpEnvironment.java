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
     * 单例
     */
    private static HttpEnvironment instance;
    /**
     * 端口号
     */
    private int port;
    /**
     * 是否重定向至https
     */
    private boolean redirectHttps;

    /**
     * 获取环境对象
     *
     * @return 环境对象
     */
    public static HttpEnvironment get() {
        return instance;
    }

    static {
        HttpEnvironment env = new HttpEnvironment();
        JSONObject obj = ConfigEnvironment.getChild("http");
        env.port = obj.getInteger("port");
        env.redirectHttps = obj.getBoolean("redirectHttps");
        HttpEnvironment.instance = env;
    }

    public int getPort() {
        return port;
    }

    public boolean isRedirectHttps() {
        return redirectHttps;
    }
}
