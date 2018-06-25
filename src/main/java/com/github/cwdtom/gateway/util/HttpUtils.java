package com.github.cwdtom.gateway.util;

import io.netty.handler.codec.http.FullHttpResponse;
import okhttp3.*;

import java.io.IOException;

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
        CLIENT = new OkHttpClient();
        // 去除并发数限制
        CLIENT.dispatcher().setMaxRequestsPerHost(Integer.MAX_VALUE);
    }

    /**
     * 发送GET请求
     *
     * @param url   目标URL
     * @param param 参数，格式：name1=value1&name2=value2
     * @return 响应结果
     */
    public static FullHttpResponse sendGet(String url, String param) throws IOException {
        return sendGet(url + "?" + param);
    }

    /**
     * 发送GET请求
     *
     * @param url 目标URL
     * @return 响应结果
     */
    public static FullHttpResponse sendGet(String url) throws IOException {
        Request request = new Request.Builder()
                .header("accept", "*/*")
                .header("connection", "Keep-Alive")
                .header("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException();
        }
        return ResponseUtils.buildResponse(response);
    }

    /**
     * 发送POST请求
     *
     * @param url   目标URL
     * @param param 参数
     * @return 响应结果
     */
    public static FullHttpResponse sendPost(String url, byte[] param, String contentType) throws IOException {
        if (param == null || contentType == null) {
            throw new IOException();
        }
        Request request = new Request.Builder()
                .header("accept", "*/*")
                .header("connection", "Keep-Alive")
                .header("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .post(RequestBody.create(MediaType.parse(contentType), param))
                .url(url).build();
        Response response = CLIENT.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException();
        }
        return ResponseUtils.buildResponse(response);
    }
}
