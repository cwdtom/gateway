package com.github.cwdtom.gateway.limit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * token bucket
 *
 * @author chenweidong
 * @since 1.3.0
 */
public class TokenBucket {
    /**
     * token count
     */
    private AtomicInteger count = new AtomicInteger(0);
    /**
     * token bucket size
     */
    private int maxSize;

    public TokenBucket(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * put token to bucket
     */
    public void offer() {
        if (count.get() < maxSize) {
            count.incrementAndGet();
        }
    }

    /**
     * get token
     *
     * @return success or fail
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
