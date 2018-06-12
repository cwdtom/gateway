package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.environment.CorsEnvironment;
import com.github.cwdtom.gateway.environment.FlowLimitsEnvironment;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.environment.MappingConfig;
import com.github.cwdtom.gateway.limit.TokenPool;
import com.github.cwdtom.gateway.mapping.Mapper;
import com.github.cwdtom.gateway.util.HttpUtils;
import com.github.cwdtom.gateway.util.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 请求处理
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class RequestHandler implements Runnable {
    /**
     * 信道
     */
    private Channel channel;
    /**
     * 请求
     */
    private FullHttpRequest request;
    /**
     * 响应
     */
    private FullHttpResponse response;
    /**
     * host
     */
    private String host;
    /**
     * 是否是https请求
     */
    private boolean isHttps;

    public RequestHandler(Channel channel, FullHttpRequest request, String host, boolean isHttps) {
        this.channel = channel;
        this.request = request;
        this.host = host;
        this.isHttps = isHttps;
    }

    @Override
    public void run() {
        Mapper mapper = MappingConfig.getMappingIsostatic(host);
        try {
            // 反向代理地址不存在
            if (mapper == null) {
                return;
            }
            // 判断是否需要重定向至https
            if (isHttps && HttpEnvironment.get().isRedirectHttps()) {
                response = ResponseUtils.buildRedirectResponse(host);
                return;
            }
            // 判断是否需要限流
            if (FlowLimitsEnvironment.get().isEnable() && !TokenPool.take()) {
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.REQUEST_TIMEOUT);
                return;
            }
            // 判断解析是否成功
            if (request.decoderResult().isFailure()) {
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST);
                return;
            }
            // 判断连接使用次数
            if (HttpUtil.is100ContinueExpected(request)) {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                return;
            }

            process(mapper.getTarget());
        } catch (Exception e) {
            log.error("server error.", e);
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY);
            log.error("{} {} offline.", host, mapper.exception(request.method(), request.uri(),
                    request.headers().get(HttpHeaderNames.CONTENT_TYPE)));
        } finally {
            if (response != null && mapper != null) {
                ChannelFuture cf = channel.writeAndFlush(response);
                if (!HttpUtil.isKeepAlive(response)) {
                    cf.addListener(ChannelFutureListener.CLOSE);
                }
            } else {
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.SERVICE_UNAVAILABLE))
                        .addListener(ChannelFutureListener.CLOSE);
            }
            // 检查请求体是否被释放
            int refCnt = request.content().refCnt();
            if (refCnt != 0) {
                request.content().release(refCnt);
            }
        }
    }

    /**
     * 处理请求
     *
     * @param mapping 映射地址
     * @throws IOException 转发失败
     */
    private void process(String mapping) throws IOException {
        String url = Constant.HTTP_PREFIX + mapping + request.uri();
        if (request.method().equals(HttpMethod.GET) && mapping != null) {
            // 处理get请求
            log.info("GET {}", url);
            response = HttpUtils.sendGet(url);
        } else if (request.method().equals(HttpMethod.POST) && mapping != null) {
            // 处理post请求
            log.info("POST {}", url);
            ByteBuf byteBuf = request.content();
            byte[] bytes = new byte[(int) HttpUtil.getContentLength(request)];
            byteBuf.readBytes(bytes);
            // 释放请求体
            request.content().release();
            response = HttpUtils.sendPost(url, bytes, request.headers().get(HttpHeaderNames.CONTENT_TYPE));
        } else if (request.method().equals(HttpMethod.OPTIONS)) {
            // 处理options请求 支持cors
            String origin = request.headers().get(HttpHeaderNames.ORIGIN);
            if (CorsEnvironment.isLegal(origin)) {
                response = ResponseUtils.buildOptionsResponse();
            } else {
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.NOT_ACCEPTABLE);
            }
        } else {
            // 不支持其他请求
            log.info("NONSUPPORT {}", url);
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST);
        }
        // 是否keep-alive
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.CLOSE.toString());
        }
    }
}
