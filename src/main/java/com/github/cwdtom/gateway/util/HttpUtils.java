package com.github.cwdtom.gateway.util;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP工具
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpUtils {
    /**
     * client
     */
    private static final OkHttpClient CLIENT;

    static {
        CLIENT = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        // 去除并发数限制
        CLIENT.dispatcher().setMaxRequestsPerHost(Integer.MAX_VALUE);
    }

    /**
     * 发送GET请求
     *
     * @param url     目标URL
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendGet(String url, HttpHeaders headers) throws IOException {
        Request request = buildRequestHeader(headers).get().url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送GET请求
     *
     * @param url 目标URL
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendGet(String url) throws IOException {
        Request request = new Request.Builder()
                .header("accept", "*/*")
                .header("connection", "Keep-Alive")
                .header("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送POST请求
     *
     * @param url     目标URL
     * @param param   数据
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendPost(String url, byte[] param, HttpHeaders headers) throws IOException {
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (param == null) {
            param = new byte[0];
            contentType = "multipart/form-data";
        }
        Request request = buildRequestHeader(headers)
                .post(RequestBody.create(MediaType.parse(contentType), param))
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送head请求
     *
     * @param url     目标url
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendHead(String url, HttpHeaders headers) throws IOException {
        Request request = buildRequestHeader(headers).head().url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送put请求
     *
     * @param url     目标URL
     * @param param   数据
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendPut(String url, byte[] param, HttpHeaders headers) throws IOException {
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (param == null) {
            param = new byte[0];
            contentType = "multipart/form-data";
        }
        Request request = buildRequestHeader(headers)
                .put(RequestBody.create(MediaType.parse(contentType), param))
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送patch请求
     *
     * @param url     目标URL
     * @param param   数据
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendPatch(String url, byte[] param, HttpHeaders headers) throws IOException {
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (param == null) {
            param = new byte[0];
            contentType = "multipart/form-data";
        }
        Request request = buildRequestHeader(headers)
                .patch(RequestBody.create(MediaType.parse(contentType), param))
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送delete请求
     *
     * @param url     目标URL
     * @param param   数据
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 请求异常
     */
    public static FullHttpResponse sendDelete(String url, byte[] param, HttpHeaders headers) throws IOException {
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (param == null) {
            param = new byte[0];
            contentType = "multipart/form-data";
        }
        Request request = buildRequestHeader(headers)
                .delete(RequestBody.create(MediaType.parse(contentType), param))
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 构造请求头
     *
     * @param headers 请求头
     * @return 请求构造器
     */
    private static Request.Builder buildRequestHeader(HttpHeaders headers) {
        Request.Builder builder = new Request.Builder();
        for (Map.Entry<String, String> entry : headers.entries()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        // 单独设置保持长连
        builder.header("connection", "Keep-Alive");
        return builder;
    }
}
