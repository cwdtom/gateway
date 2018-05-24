package com.github.cwdtom.gateway.util;

import io.netty.handler.codec.http.FullHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP工具
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpUtils {
    /**
     * 设置常用请求头
     *
     * @param connection 连接实体
     * @return 连接实体
     */
    private static URLConnection setDefaultHeaders(URLConnection connection) {
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        return connection;
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
        URL realUrl = new URL(url);
        // 打开URL连接
        URLConnection connection = realUrl.openConnection();
        connection = setDefaultHeaders(connection);
        // 打开连接
        connection.connect();
        return ResponseUtils.buildResponse(connection);
    }

    /**
     * 发送POST请求
     *
     * @param url   目标URL
     * @param param 参数
     * @return 响应结果
     */
    public static FullHttpResponse sendPost(String url, byte[] param, String contentType) throws IOException {
        URL realUrl = new URL(url);
        // 打开URL连接
        URLConnection conn = realUrl.openConnection();
        conn = setDefaultHeaders(conn);
        conn.setRequestProperty("Content-Type", contentType);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(param);
            out.flush();
        }
        return ResponseUtils.buildResponse(conn);
    }
}
