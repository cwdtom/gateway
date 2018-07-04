package com.github.cwdtom.gateway.util;

import com.github.cwdtom.gateway.constant.Constant;
import eu.medsea.mimeutil.MimeUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 响应工具
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class ResponseUtils {
    /**
     * 构造失败响应
     *
     * @param status 状态码
     * @return 失败响应
     */
    public static FullHttpResponse buildFailResponse(HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }

    /**
     * 构造成功响应
     *
     * @param content     内容
     * @param contentType 内容类型
     * @return 成功响应
     */
    public static FullHttpResponse buildSuccessResponse(String content, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content.getBytes()));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE.toString(), contentType)
                .set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }

    /**
     * 构造响应
     *
     * @param resp 响应内容
     * @return 响应
     */
    public static FullHttpResponse buildResponse(Response resp) throws IOException {
        try {
            FullHttpResponse response;
            ResponseBody responseBody  = resp.body();
            if (responseBody == null) {
                response = new DefaultFullHttpResponse(HttpVersion.valueOf(resp.protocol().toString()),
                        HttpResponseStatus.valueOf(resp.code()));
            } else {
                response = new DefaultFullHttpResponse(HttpVersion.valueOf(resp.protocol().toString()),
                        HttpResponseStatus.valueOf(resp.code()),
                        Unpooled.wrappedBuffer(responseBody.bytes()));
            }
            for (Map.Entry<String, List<String>> entry : resp.headers().toMultimap().entrySet()) {
                response.headers().set(entry.getKey(), entry.getValue());
            }
            return response;
        } finally {
            resp.close();
        }
    }

    /**
     * 构造响应
     *
     * @param file    文件
     * @param version http版本
     * @return 响应
     */
    public static FullHttpResponse buildResponse(File file, HttpVersion version) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(version, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(Files.readAllBytes(file.toPath())));
        Collection<?> mimeTypes = MimeUtil.getMimeTypes(file);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE.toString(), mimeTypes)
                .set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }

    /**
     * 构造重定向响应
     *
     * @param host 重定向host
     * @return 响应
     */
    public static FullHttpResponse buildRedirectResponse(String host) {
        byte[] content = String.format(Constant.REDIRECT_TEMPLATE, Constant.HTTPS_PREFIX + host).getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE.toString(), "text/html")
                .set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }

    /**
     * 构造预校验响应体
     *
     * @return 响应
     */
    public static FullHttpResponse buildOptionsResponse() {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), "GET, POST")
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(), HttpHeaderNames.CONTENT_TYPE)
                .set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }
}
