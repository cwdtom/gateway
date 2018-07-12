package com.github.cwdtom.gateway.constant;

/**
 * http constant
 *
 * @author chenweidong
 * @since 3.0.2
 */
public class HttpConstant {
    /**
     * the max request size,10MB
     */
    public static final int MAX_CONTENT_LEN = 64 * 1024 * 1024;
    /**
     * http pre
     */
    public static final String HTTP_PREFIX = "http://";
    /**
     * https pre
     */
    public static final String HTTPS_PREFIX = "https://";
    /**
     * redirect template
     */
    public static final String REDIRECT_TEMPLATE = "<html><meta http-equiv='refresh' content='0;url=%s'></html>";
}
