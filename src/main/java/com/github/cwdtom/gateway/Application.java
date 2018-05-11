package com.github.cwdtom.gateway;

import com.github.cwdtom.gateway.listener.HttpListener;

/**
 * 启动类
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class Application {
    public static void main(String[] args) {
        new HttpListener().listen();
    }
}
