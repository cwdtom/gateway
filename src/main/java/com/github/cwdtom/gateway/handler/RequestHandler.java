package com.github.cwdtom.gateway.handler;

import com.github.cwdtom.gateway.entity.Constant;
import com.github.cwdtom.gateway.entity.RequestTask;
import com.github.cwdtom.gateway.environment.HttpEnvironment;
import com.github.cwdtom.gateway.environment.MappingConfig;
import com.github.cwdtom.gateway.util.HttpUtils;
import com.github.cwdtom.gateway.util.ResponseUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;


/**
 * 请求处理
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
@AllArgsConstructor
public class RequestHandler implements Runnable {
    /**
     * 请求任务队列
     */
    private BlockingQueue<RequestTask> queue;

    @Override
    public void run() {
        while (true) {
            FullHttpRequest request = null;
            Channel channel = null;
            try {
                RequestTask task = queue.take();
                request = task.getRequest();
                channel = task.getChannel();
                // 判断解析是否成功
                if (request.decoderResult().isFailure()) {
                    channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST));
                    return;
                }

                String host = request.headers().get(HttpHeaderNames.HOST);
                // 判断是否需要重定向至https
                if (HttpEnvironment.get().isRedirectHttps()) {
                    channel.writeAndFlush(ResponseUtils.buildRedirectResponse(host));
                    return;
                }

                String mapping = MappingConfig.getMappingIsostatic(host);
                if (request.method().equals(HttpMethod.GET) && mapping != null) {
                    // 处理get请求
                    channel.writeAndFlush(HttpUtils.sendGet(Constant.HTTP_PREFIX + mapping + request.uri()));
                } else if (request.method().equals(HttpMethod.POST) && mapping != null) {
                    // 处理post请求
                    ByteBuf byteBuf = request.content();
                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);
                    channel.writeAndFlush(HttpUtils.sendPost(Constant.HTTP_PREFIX + mapping, bytes,
                            request.headers().get(HttpHeaderNames.CONTENT_TYPE)));
                } else {
                    // 不支持其他请求
                    channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_REQUEST));
                }
            } catch (IOException ie) {
                log.warn("request fail.", ie);
                channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.BAD_GATEWAY));
            } catch (Exception e) {
                log.error("server error.", e);
                if (channel != null) {
                    channel.writeAndFlush(ResponseUtils.buildFailResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                }
            } finally {
                if (request != null && channel != null) {
                    String connection = request.headers().get(HttpHeaderNames.CONNECTION);
                    if (connection == null || !Constant.KEEP_ALIVE.equals(connection)) {
                        channel.close();
                    }
                }
            }
        }
    }
}
