package com.github.cwdtom.gateway.listener;

import com.github.cwdtom.gateway.entity.Constant;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.handler.HttpHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Http监听
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpListener {
    /**
     * 开始监听 阻塞
     */
    public void listen() {
        HttpEnvironment evn = HttpEnvironment.get();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast(new HttpResponseEncoder());
                p.addLast(new HttpRequestDecoder());
                p.addLast(new HttpObjectAggregator(Constant.MAX_CONTENT_LEN));
                p.addLast(new HttpHandler());
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.bind(evn.getPort()).sync();
            if (channelFuture.isSuccess()) {
                log.info("gateway is started in {}.", evn.getPort());
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("netty listen fail.", e);
            System.exit(1);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
