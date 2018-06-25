package com.github.cwdtom.gateway.listener;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.handler.HttpHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Http监听
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpListener implements Runnable {
    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;
    /**
     * boss线程池
     */
    private final EventLoopGroup boss;
    /**
     * worker线程池
     */
    private final EventLoopGroup worker;
    /**
     * 信道类
     */
    private final Class<? extends ServerChannel> channelClass;

    public HttpListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        if (Constant.LINUX.equals(System.getProperty(Constant.OS_NAME))) {
            boss = new EpollEventLoopGroup();
            worker = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
            log.info("http listener used epoll model");
        } else {
            boss = new NioEventLoopGroup();
            worker = new NioEventLoopGroup();
            channelClass = NioServerSocketChannel.class;
            log.info("http listener used nio model");
        }
    }

    /**
     * 关闭
     */
    public void shutdown() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }

    /**
     * 开始监听 阻塞
     */
    private void listen() {
        HttpEnvironment env = applicationContext.getContext(HttpEnvironment.class);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.channel(channelClass);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast(new HttpResponseEncoder());
                p.addLast(new HttpRequestDecoder());
                p.addLast(new IdleStateHandler(0, 0, 20));
                p.addLast(new HttpContentCompressor());
                p.addLast(new HttpObjectAggregator(Constant.MAX_CONTENT_LEN));
                p.addLast(new HttpHandler(applicationContext, false));
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.bind(env.getPort()).sync();
            if (channelFuture.isSuccess()) {
                log.info("http is started in {}.", env.getPort());
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("http listen fail.", e);
        } finally {
            shutdown();
        }
    }

    @Override
    public void run() {
        listen();
    }
}
