package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.entity.RequestTask;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

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
     * 请求任务队列
     */
    private BlockingQueue<RequestTask> queue;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        queue.offer(new RequestTask(channelHandlerContext.channel(), request));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(ctx.channel().remoteAddress().toString(), cause);
        ctx.close();
    }
}
