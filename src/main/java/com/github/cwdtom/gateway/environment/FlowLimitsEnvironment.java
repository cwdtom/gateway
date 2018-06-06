package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.limit.TokenPool;

/**
 * 限流环境
 *
 * @author chenweidong
 * @since 1.3.0
 */
public class FlowLimitsEnvironment {
    /**
     * 单例
     */
    private static FlowLimitsEnvironment instance;
    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * 超时时间ms
     */
    private long timeout;
    /**
     * 令牌产生速率ms
     */
    private long rate;
    /**
     * 令牌池最大大小
     */
    private int maxSize;

    static {
        FlowLimitsEnvironment env = new FlowLimitsEnvironment();
        JSONObject obj = ConfigEnvironment.getChild("flowLimits");
        env.enable = obj.getBoolean("enable");
        env.timeout = obj.getLong("timeout");
        env.rate = obj.getLong("rate");
        env.maxSize = obj.getInteger("maxSize");
        instance = env;
        // 初始化令牌池
        TokenPool.initializeTokenPool(env);
    }

    public static FlowLimitsEnvironment get() {
        return instance;
    }

    public boolean isEnable() {
        return enable;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getRate() {
        return rate;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
