package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import com.github.cwdtom.gateway.util.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * http handler
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    /**
     * 是否是https请求
     */
    private boolean isHttps;
    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;

    public HttpHandler(ApplicationContext applicationContext, boolean isHttps) {
        this.isHttps = isHttps;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        String host = request.headers().get(HttpHeaderNames.HOST);
        ByteBuf byteBuf = request.content();
        int len = (int) HttpUtil.getContentLength(request);
        byte[] bytes = null;
        if (len > 0) {
            bytes = new byte[len];
            byteBuf.readBytes(bytes);
            byteBuf.discardReadBytes();
        }
        applicationContext.getContext(ThreadPoolGroup.class).execute(host,
                new RequestHandler(channelHandlerContext.channel(), request, host, isHttps, bytes, applicationContext));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception.", cause);
        ctx.channel().writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR))
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 超时关闭
        ctx.close();
    }
}
