package com.github.cwdtom.gateway.limit;

import com.github.cwdtom.gateway.environment.FlowLimitsEnvironment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 令牌池
 *
 * @author chenweidong
 * @since 1.3.0
 */
public class TokenPool {
    /**
     * 单例
     */
    private static TokenPool instance;
    /**
     * 是否初始化，只允许初始化一次
     */
    private static boolean isInit = false;
    /**
     * 令牌数量
     */
    private AtomicInteger count;
    /**
     * 令牌桶大小
     */
    private int maxSize;

    /**
     * 初始化
     *
     * @param env 限流环境
     */
    public static synchronized void initializeTokenPool(FlowLimitsEnvironment env) {
        if (!isInit) {
            TokenPool tokenPool = new TokenPool();
            tokenPool.count = new AtomicInteger(0);
            tokenPool.maxSize = env.getMaxSize();
            instance = tokenPool;
            isInit = true;
        }
    }

    /**
     * 放入令牌
     */
    public static void offer() {
        if (instance.count.get() < instance.maxSize) {
            instance.count.incrementAndGet();
        }
    }

    /**
     * 获取令牌
     *
     * @return 是否获取成功
     */
    public static boolean take() {
        if (instance.count.getAndDecrement() >= 0) {
            return true;
        } else {
            instance.count.getAndIncrement();
            return false;
        }
    }
}
