package com.github.cwdtom.gateway.filter;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 前置拦截器
 *
 * @author chenweidong
 * @since 2.2.0
 */
public interface BeforeFilter {
    /**
     * 过滤器
     *
     * @param request 请求体
     * @param content 内容
     * @return 是否继续执行
     */
    boolean filter(FullHttpRequest request, byte[] content);
}
