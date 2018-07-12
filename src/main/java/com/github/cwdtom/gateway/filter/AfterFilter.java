package com.github.cwdtom.gateway.filter;

import io.netty.handler.codec.http.FullHttpResponse;

/**
 * post filter
 *
 * @author chenweidong
 * @since 2.2.0
 */
public interface AfterFilter {
    /**
     * filter
     *
     * @param response response
     */
    void filter(FullHttpResponse response);
}
