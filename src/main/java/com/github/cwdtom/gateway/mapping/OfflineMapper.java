package com.github.cwdtom.gateway.mapping;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 熔断mapper
 *
 * @author chenweidong
 * @since 1.4.0
 */
@AllArgsConstructor
@Data
class OfflineMapper {
    /**
     * 映射
     */
    private Mapper mapper;
    /**
     * 类型
     */
    private HttpMethod method;
    /**
     * uri
     */
    private String uri;
    /**
     * 消息类型
     */
    private String contentType;
}
