package com.github.cwdtom.gateway.entity;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求任务
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
public class RequestTask {
    /**
     * 信道
     */
    private Channel channel;
    /**
     * 请求
     */
    private FullHttpRequest request;
}
