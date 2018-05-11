package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.environment.ThreadPool;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

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
        ThreadPool.execute(new RequestHandler(channelHandlerContext.channel(), request));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ctx.channel().remoteAddress().toString(), cause);
        ctx.close();
    }
}
