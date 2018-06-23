package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.environment.ThreadPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * http handler
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
@AllArgsConstructor
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    /**
     * 是否是https请求
     */
    private boolean isHttps;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        String host = request.headers().get(HttpHeaderNames.HOST);
        // 保留请求体
        request.content().retain();
        ThreadPool.execute(host, new RequestHandler(channelHandlerContext.channel(), request, host, isHttps));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof RuntimeException)) {
            log.error(ctx.channel().remoteAddress().toString(), cause);
        }
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 超时关闭
        ctx.close();
    }
}
