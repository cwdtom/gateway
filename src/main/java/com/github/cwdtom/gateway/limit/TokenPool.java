package com.github.cwdtom.gateway.limit;

import com.github.cwdtom.gateway.environment.FlowLimitsEnvironment;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
     * 令牌队列
     */
    private BlockingQueue<Integer> pool;
    /**
     * 等待超时时间
     */
    private long timeout;

    /**
     * 初始化
     *
     * @param env 限流环境
     */
    public static synchronized void initializeTokenPool(FlowLimitsEnvironment env) {
        if (!isInit) {
            TokenPool tokenPool = new TokenPool();
            tokenPool.pool = new ArrayBlockingQueue<>(env.getMaxSize());
            tokenPool.timeout = env.getTimeout();
            instance = tokenPool;
            isInit = true;
        }
    }

    /**
     * 放入令牌
     */
    public static void offer() {
        instance.pool.offer(1);
    }

    /**
     * 获取令牌
     *
     * @return 是否获取成功
     */
    public static boolean take() {
        try {
            Integer token = instance.pool.poll(instance.timeout, TimeUnit.MILLISECONDS);
            return token != null;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
