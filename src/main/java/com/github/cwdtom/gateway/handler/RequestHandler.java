package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.entity.Constant;
import com.github.cwdtom.gateway.environment.CorsEnvironment;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.environment.MappingConfig;
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

    public RequestHandler(Channel channel, FullHttpRequest request) {
        this.channel = channel;
        this.request = request;
    }

    @Override
    public void run() {
        try {
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
            String host = request.headers().get(HttpHeaderNames.HOST);
            // 判断是否需要重定向至https
            if (HttpEnvironment.get().isRedirectHttps()) {
                response = ResponseUtils.buildRedirectResponse(host);
                return;
            }
            process(host);
        } catch (Exception e) {
            log.error("server error.", e);
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY);
        } finally {
            if (response != null) {
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
     * @param host host
     * @throws IOException 转发失败
     */
    private void process(String host) throws IOException {
        String mapping = MappingConfig.getMappingIsostatic(host);
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
