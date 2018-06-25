package com.github.cwdtom.gateway.limit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 令牌池
 *
 * @author chenweidong
 * @since 1.3.0
 */
public class TokenBucket {
    /**
     * 令牌数量
     */
    private AtomicInteger count = new AtomicInteger(0);
    /**
     * 令牌桶大小
     */
    private int maxSize;

    /**
     * 初始化
     *
     * @param maxSize 令牌桶大小
     */
    public TokenBucket(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * 放入令牌
     */
    public void offer() {
        if (count.get() < maxSize) {
            count.incrementAndGet();
        }
    }

    /**
     * 获取令牌
     *
     * @return 是否获取成功
     */
    public boolean take() {
        if (count.getAndDecrement() >= 0) {
            return true;
        } else {
            count.getAndIncrement();
            return false;
        }
    }
}
