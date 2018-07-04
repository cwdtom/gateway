package com.github.cwdtom.gateway.constant;

/**
 * http相关常量
 *
 * @author chenweidong
 * @since 3.0.2
 */
public class HttpConstant {
    /**
     * 最大单个请求大小，10MB
     */
    public static final int MAX_CONTENT_LEN = 64 * 1024 * 1024;
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
}
