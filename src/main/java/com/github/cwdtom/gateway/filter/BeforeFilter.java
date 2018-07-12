package com.github.cwdtom.gateway.filter;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * pre filter
 *
 * @author chenweidong
 * @since 2.2.0
 */
public interface BeforeFilter {
    /**
     * filter
     *
     * @param request netty http request
     * @param content http request context
     * @return continue or not
     */
    boolean filter(FullHttpRequest request, byte[] content);
}
