package com.github.cwdtom.gateway.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认线程工厂
 *
 * @author chenweidong
 * @since 1.7.2
 */
public class DefaultThreadFactory implements ThreadFactory {
    /**
     * host
     */
    private String host;
    /**
     * 原子数
     */
    private AtomicInteger count = new AtomicInteger(0);

    public DefaultThreadFactory(String host) {
        this.host = host;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "gateway-" + host + "-" + count.incrementAndGet());
    }
}
