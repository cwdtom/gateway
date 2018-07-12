package com.github.cwdtom.gateway.util;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * http request utils
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class HttpUtils {
    /**
     * okhttp client
     */
    private static final OkHttpClient CLIENT;

    static {
        CLIENT = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        // remove concurrency restrictions
        CLIENT.dispatcher().setMaxRequestsPerHost(Integer.MAX_VALUE);
    }

    /**
     * send get request
     *
     * @param url     target url
     * @param headers http headers
     * @return response
     * @throws IOException network error
     */
    public static FullHttpResponse sendGet(String url, HttpHeaders headers) throws IOException {
        Request request = buildRequestHeader(headers).get().url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * send get request
     *
     * @param url target url
     * @return response
     * @throws IOException network error
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
     * send post request
     *
     * @param url     target url
     * @param param   http content
     * @param headers http headers
     * @return response
     * @throws IOException network error
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
     * send head request
     *
     * @param url     target url
     * @param headers http headers
     * @return response
     * @throws IOException network error
     */
    public static FullHttpResponse sendHead(String url, HttpHeaders headers) throws IOException {
        Request request = buildRequestHeader(headers).head().url(url).build();
        Response response = CLIENT.newCall(request).execute();
        return ResponseUtils.buildResponse(response);
    }

    /**
     * send put request
     *
     * @param url     target url
     * @param param   http content
     * @param headers http headers
     * @return response
     * @throws IOException network error
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
     * send patch request
     *
     * @param url     target url
     * @param param   http content
     * @param headers http headers
     * @return response
     * @throws IOException network error
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
     * send delete request
     *
     * @param url     target url
     * @param param   http content
     * @param headers http headers
     * @return response
     * @throws IOException network error
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
     * build http headers
     *
     * @param headers netty http headers
     * @return okhttp request builder
     */
    private static Request.Builder buildRequestHeader(HttpHeaders headers) {
        Request.Builder builder = new Request.Builder();
        for (Map.Entry<String, String> entry : headers.entries()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        // set connection keep-alive
        builder.header("connection", "Keep-Alive");
        return builder;
    }
}
