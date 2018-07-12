package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.constant.HttpConstant;
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
 * request handler
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class RequestHandler implements Runnable {
    /**
     * application context
     */
    private final ApplicationContext applicationContext;
    /**
     * channel
     */
    private Channel channel;
    /**
     * request
     */
    private FullHttpRequest request;
    /**
     * response
     */
    private FullHttpResponse response;
    /**
     * host
     */
    private String host;
    /**
     * https or http
     */
    private boolean isHttps;
    /**
     * request content
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
            // check for flow limits
            FlowLimitsEnvironment fle = applicationContext.getContext(FlowLimitsEnvironment.class);
            if (fle.isEnable() && !fle.getTokenBucket().take()) {
                log.info("FLOW LIMIT {} {}.", host, request.uri());
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.REQUEST_TIMEOUT);
                return;
            }
            String ip = channel.remoteAddress().toString().split(":")[0];
            mapper = applicationContext.getContext(MappingEnvironment.class).getLoadBalance(host, ip);
            // mapper is not exist
            if (mapper == null) {
                String path = applicationContext.getContext(LocalFileEnvironment.class).getPath(host);
                if (path != null) {
                    // return static resource,prevent cross directory access
                    path += request.uri().replace("/../", "/");
                    File file = new File(path);
                    if (file.exists() && file.isFile()) {
                        log.info("GET STATIC RESOURCE {}", file.getPath());
                        response = ResponseUtils.buildResponse(file, request.protocolVersion());
                    }
                }
                return;
            }
            // redirect https host or not
            if (isHttps && applicationContext.getContext(HttpEnvironment.class).isRedirectHttps()) {
                response = ResponseUtils.buildRedirectResponse(host);
                return;
            }
            // decode success or fail
            if (request.decoderResult().isFailure()) {
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST);
                return;
            }
            // check for connection count
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
     * help collect resource
     *
     * @param isClose release channel or not
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
     * process request
     *
     * @param mapping target proxy host
     * @throws IOException network error
     */
    private void process(String mapping) throws IOException {
        FilterEnvironment filter = applicationContext.getContext(FilterEnvironment.class);
        // pre filter
        if (!filter.beforeFilter(request, content)) {
            response = ResponseUtils.buildFailResponse(HttpResponseStatus.NOT_ACCEPTABLE);
            return;
        }

        String url = HttpConstant.HTTP_PREFIX + mapping + request.uri();
        log.info(request.method().toString() + " {}", url);
        if (mapping != null) {
            if (request.method().equals(HttpMethod.GET)) {
                // GET
                response = HttpUtils.sendGet(url, request.headers());
            } else if (request.method().equals(HttpMethod.POST)) {
                // POST
                response = HttpUtils.sendPost(url, content, request.headers());
            } else if (request.method().equals(HttpMethod.OPTIONS)) {
                // OPTIONS support cors
                String origin = request.headers().get(HttpHeaderNames.ORIGIN);
                if (applicationContext.getContext(CorsEnvironment.class).isLegal(origin)) {
                    response = ResponseUtils.buildOptionsResponse(origin);
                    response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                            applicationContext.getContext(CorsEnvironment.class).getAllowMethods());
                } else {
                    response = ResponseUtils.buildFailResponse(HttpResponseStatus.NOT_ACCEPTABLE);
                }
            } else if (request.method().equals(HttpMethod.HEAD)) {
                // HEAD
                response = HttpUtils.sendHead(url, request.headers());
            } else if (request.method().equals(HttpMethod.PUT)) {
                // PUT
                response = HttpUtils.sendPut(url, content, request.headers());
            } else if (request.method().equals(HttpMethod.PATCH)) {
                // PATCH
                response = HttpUtils.sendPatch(url, content, request.headers());
            } else if (request.method().equals(HttpMethod.DELETE)) {
                // DELETE
                response = HttpUtils.sendDelete(url, content, request.headers());
            } else {
                // nonsupport others
                response = ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST);
            }
        }
        // keep-alive or not
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.CLOSE.toString());
        }

        // post filter
        filter.afterFilter(response);
    }
}
