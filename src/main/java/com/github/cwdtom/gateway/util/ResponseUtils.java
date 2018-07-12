package com.github.cwdtom.gateway.util;

import com.github.cwdtom.gateway.constant.HttpConstant;
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
 * build response utils
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class ResponseUtils {
    /**
     * build fail response
     *
     * @param status http status
     * @return response
     */
    public static FullHttpResponse buildFailResponse(HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }

    /**
     * build response
     *
     * @param resp okhttp http response
     * @return netty http response
     * @throws IOException network error
     */
    public static FullHttpResponse buildResponse(Response resp) throws IOException {
        try {
            FullHttpResponse response;
            ResponseBody responseBody = resp.body();
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
     * build response
     *
     * @param file    local file
     * @param version http version
     * @return response
     * @throws IOException read local file error
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
     * build redirect response
     *
     * @param host redirect host
     * @return response
     */
    public static FullHttpResponse buildRedirectResponse(String host) {
        byte[] content = String.format(HttpConstant.REDIRECT_TEMPLATE, HttpConstant.HTTPS_PREFIX + host).getBytes();
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE.toString(), "text/html")
                .set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }

    /**
     * build options response
     *
     * @param origin http origin host
     * @return response
     */
    public static FullHttpResponse buildOptionsResponse(String origin) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), origin == null ? "*" : origin)
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(), HttpHeaderNames.CONTENT_TYPE)
                .set(HttpHeaderNames.CONTENT_LENGTH.toString(), response.content().readableBytes());
        return response;
    }
}
