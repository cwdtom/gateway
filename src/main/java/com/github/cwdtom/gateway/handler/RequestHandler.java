package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.entity.Constant;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.environment.MappingConfig;
import com.github.cwdtom.gateway.util.HttpUtils;
import com.github.cwdtom.gateway.util.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 请求处理
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
@AllArgsConstructor
public class RequestHandler implements Runnable {
    /**
     * 信道
     */
    private Channel channel;
    /**
     * 请求
     */
    private FullHttpRequest request;

    @Override
    public void run() {
        FullHttpResponse response = null;
        boolean isKeepAlive = false;
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

            String mapping = MappingConfig.getMappingIsostatic(host);
            String url = Constant.HTTP_PREFIX + mapping + request.uri();
            isKeepAlive = HttpUtil.isKeepAlive(request);
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
            } else {
                // 不支持其他请求
                log.info("NONSUPPORT {}", url);
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST);
            }
        } catch (IOException ie) {
            log.warn("request fail.", ie);
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY);
            isKeepAlive = false;
        } catch (Exception e) {
            log.error("server error.", e);
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            isKeepAlive = false;
        } finally {
            if (response != null) {
                if (isKeepAlive) {
                    response.headers().set(HttpHeaderNames.CONNECTION.toString(),
                            HttpHeaderValues.KEEP_ALIVE.toString());
                    channel.writeAndFlush(response);
                } else {
                    response.headers().set(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.CLOSE.toString());
                    channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            } else {
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.SERVICE_UNAVAILABLE))
                        .addListener(ChannelFutureListener.CLOSE);
            }
            // 检查请求体是否被释放
            if (request.content().refCnt() != 0) {
                request.content().release();
            }
        }
    }
}
