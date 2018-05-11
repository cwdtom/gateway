package com.github.cwdtom.gateway.entity;

/**
 * 常量
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class Constant {
    /**
     * 配置文件地址
     */
    public static final String CONFIG_FILE_PATH = "/application.yml";
    /**
     * 最大单个请求大小，10MB
     */
    public static final int MAX_CONTENT_LEN = 10 * 1024 * 1024;
    /**
     * http前缀
     */
    public static final String HTTP_PREFIX = "http://";
    /**
     * https前缀
     */
    public static final String HTTPS_PREFIX = "https://";
    /**
     * 重定向响应体模板
     */
    public static final String REDIRECT_TEMPLATE = "<html><meta http-equiv='refresh' content='0;url=%s'></html>";
    /**
     * connection长连value
     */
    public static final String KEEP_ALIVE = "keep-alive";
}
