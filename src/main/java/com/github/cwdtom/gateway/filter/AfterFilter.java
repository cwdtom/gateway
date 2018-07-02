package com.github.cwdtom.gateway.filter;

import io.netty.handler.codec.http.FullHttpResponse;

/**
 * 后置过滤器
 *
 * @author chenweidong
 * @since 2.2.0
 */
public interface AfterFilter {
    /**
     * 过滤器
     *
     * @param response 响应
     */
    void filter(FullHttpResponse response);
}
