package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.environment.*;
import com.github.cwdtom.gateway.mapping.Mapper;
import com.github.cwdtom.gateway.util.HttpUtils;
import com.github.cwdtom.gateway.util.ResponseUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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
     * 应用上下文
     */
    private final ApplicationContext applicationContext;
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
    /**
     * host
     */
    private String host;
    /**
     * 是否是https请求
     */
    private boolean isHttps;
    /**
     * 请求体
     */
    private byte[] content;

    public RequestHandler(Channel channel, FullHttpRequest request, String host,
                          boolean isHttps, byte[] content, ApplicationContext applicationContext) {
        this.channel = channel;
        this.request = request;
        this.host = host;
        this.isHttps = isHttps;
        this.content = content;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        Mapper mapper = null;
        try {
            // 判断是否需要限流
            FlowLimitsEnvironment fle = applicationContext.getContext(FlowLimitsEnvironment.class);
            if (fle.isEnable() && !fle.getTokenBucket().take()) {
                log.info("FLOW LIMIT {} {}.", host, request.uri());
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.REQUEST_TIMEOUT);
                return;
            }
            String ip = channel.remoteAddress().toString().split(":")[0];
            mapper = applicationContext.getContext(MappingEnvironment.class).getLoadBalance(host, ip);
            // 反向代理地址不存在
            if (mapper == null) {
                String path = applicationContext.getContext(StaticEnvironment.class).getPath(host);
                if (path != null) {
                    // 返回静态资源 防止跨目录访问
                    path += request.uri().replace("/../", "/");
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        log.info("GET STATIC RESOURCE {}", file.getPath());
                        response = ResponseUtils.buildResponse(file, request.protocolVersion());
                    }
                }
                return;
            }
            // 判断是否需要重定向至https
            if (isHttps && applicationContext.getContext(HttpEnvironment.class).isRedirectHttps()) {
                response = ResponseUtils.buildRedirectResponse(host);
                return;
            }
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

            process(mapper.getTarget());
        } catch (Exception e) {
            log.error("server error.", e);
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY);
            if (mapper == null) {
                log.error("{} {} static resource is unable.", host, request.uri());
            } else {
                mapper.exception();
            }
        } finally {
            if (response != null) {
                ChannelFuture cf = channel.writeAndFlush(response);
                if (!HttpUtil.isKeepAlive(response)) {
                    cf.addListener(ChannelFutureListener.CLOSE);
                }
            } else {
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.NOT_FOUND))
                        .addListener(ChannelFutureListener.CLOSE);
            }
            release(false);
        }
    }

    /**
     * 手动帮助释放资源
     *
     * @param isClose 释放关闭信道
     */
    public void release(boolean isClose) {
        if (isClose) {
            channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.SERVICE_UNAVAILABLE))
                    .addListener(ChannelFutureListener.CLOSE);
        }
        channel = null;
        request = null;
        response = null;
        content = null;
    }

    /**
     * 处理请求
     *
     * @param mapping 映射地址
     * @throws IOException 转发失败
     */
    private void process(String mapping) throws IOException {
        FilterEnvironment filter = applicationContext.getContext(FilterEnvironment.class);
        // 前置过滤器
        if (!filter.beforeFilter(request, content)) {
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.NOT_ACCEPTABLE);
            return;
        }

        String url = Constant.HTTP_PREFIX + mapping + request.uri();
        if (request.method().equals(HttpMethod.GET) && mapping != null) {
            // 处理get请求
            log.info("GET {}", url);
            response = HttpUtils.sendGet(url);
        } else if (request.method().equals(HttpMethod.POST) && mapping != null) {
            // 处理post请求
            log.info("POST {}", url);
            response = HttpUtils.sendPost(url, content, request.headers().get(HttpHeaderNames.CONTENT_TYPE));
        } else if (request.method().equals(HttpMethod.OPTIONS)) {
            // 处理options请求 支持cors
            String origin = request.headers().get(HttpHeaderNames.ORIGIN);
            if (applicationContext.getContext(CorsEnvironment.class).isLegal(origin)) {
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

        // 后置过滤器
        filter.afterFilter(response);
    }
}
