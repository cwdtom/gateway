package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.entity.Constant;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.environment.MappingConfig;
import com.github.cwdtom.gateway.environment.ThreadPool;
import com.github.cwdtom.gateway.util.HttpUtils;
import com.github.cwdtom.gateway.util.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * http handler
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        // 判断解析是否成功
        if (request.decoderResult().isFailure()) {
            channelHandlerContext.channel().writeAndFlush(
                    ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST));
            return;
        }

        String host = request.headers().get(HttpHeaderNames.HOST);
        // 判断是否需要重定向至https
        if (HttpEnvironment.get().isRedirectHttps()) {
            channelHandlerContext.channel().writeAndFlush(
                    ResponseUtils.buildRedirectResponse(host));
            return;
        }

        String mapping = MappingConfig.getMappingIsostatic(host);
        if (request.method().equals(HttpMethod.GET) && mapping != null) {
            // 处理get请求
            ThreadPool.execute(new GetHandler(channelHandlerContext.channel(),
                    Constant.HTTP_PREFIX + mapping + request.uri()));
        } else if (request.method().equals(HttpMethod.POST) && mapping != null) {
            // 处理post请求
            ByteBuf byteBuf = request.content();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            ThreadPool.execute(new PostHandler(channelHandlerContext.channel(),
                    Constant.HTTP_PREFIX + mapping, bytes, request.headers().get(HttpHeaderNames.CONTENT_TYPE)));
        } else {
            // 不支持其他请求
            channelHandlerContext.channel().writeAndFlush(
                    ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ctx.channel().remoteAddress().toString(), cause);
        ctx.close();
    }

    /**
     * 转发get请求
     */
    @AllArgsConstructor
    private class GetHandler implements Runnable {
        /**
         * 信道
         */
        private Channel channel;
        /**
         * url
         */
        private String url;

        @Override
        public void run() {
            try {
                channel.writeAndFlush(HttpUtils.sendGet(url));
            } catch (IOException ie) {
                log.warn("get fail.", ie);
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY));
            } catch (Exception e) {
                log.error("get fail.", e);
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
            }
        }
    }

    /**
     * 转发post请求
     */
    @AllArgsConstructor
    private class PostHandler implements Runnable {
        /**
         * 信道
         */
        private Channel channel;
        /**
         * url
         */
        private String url;
        /**
         * 请求体bytes
         */
        private byte[] content;
        /**
         * 请求体类型
         */
        private String contentType;

        @Override
        public void run() {
            try {
                channel.writeAndFlush(HttpUtils.sendPost(url, content, contentType));
            } catch (IOException ie) {
                log.warn("post fail.", ie);
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY));
            } catch (Exception e) {
                log.error("post fail.", e);
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
            }
        }
    }
}
