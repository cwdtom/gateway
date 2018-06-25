package com.github.cwdtom.gateway.listener;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.environment.HttpsEnvironment;
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
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Https监听
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpsListener implements Runnable {
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

    public HttpsListener(ApplicationContext applicationContext) {
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
        HttpsEnvironment env = applicationContext.getContext(HttpsEnvironment.class);
        if (!env.isEnable()) {
            return;
        }
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.channel(channelClass);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                SSLEngine sslEngine = null;
                try {
                    sslEngine = sslContext(env.getKeyPwd(), env.getKeyPath()).createSSLEngine();
                } catch (Exception e) {
                    log.error("ssl key file exception.", e);
                    System.exit(1);
                }
                sslEngine.setUseClientMode(false);
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast(new SslHandler(sslEngine));
                p.addLast(new HttpResponseEncoder());
                p.addLast(new HttpRequestDecoder());
                p.addLast(new IdleStateHandler(0, 0, 20));
                p.addLast(new HttpContentCompressor());
                p.addLast(new HttpObjectAggregator(Constant.MAX_CONTENT_LEN));
                p.addLast(new HttpHandler(applicationContext, true));
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.bind(env.getPort()).sync();
            if (channelFuture.isSuccess()) {
                log.info("https is started in {}.", env.getPort());
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("https listen fail.", e);
        } finally {
            shutdown();
        }
    }

    /**
     * 获取ssl context
     *
     * @param keyPath 证书路径
     * @param pwd     证书密码
     * @return ssl context
     * @throws Exception 获取异常
     */
    private SSLContext sslContext(String pwd, String keyPath) throws Exception {
        char[] passArray = pwd.toCharArray();
        SSLContext sslContext = SSLContext.getInstance("TLSv1");
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream inputStream = new FileInputStream(keyPath);
        ks.load(inputStream, passArray);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passArray);
        sslContext.init(kmf.getKeyManagers(), null, null);
        inputStream.close();
        return sslContext;
    }

    @Override
    public void run() {
        listen();
    }
}
