package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.limit.TokenBucket;

/**
 * 限流环境
 *
 * @author chenweidong
 * @since 1.3.0
 */
public class FlowLimitsEnvironment {
    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * 令牌产生速率ms
     */
    private long rate;
    /**
     * 令牌桶
     */
    private TokenBucket tokenBucket;

    FlowLimitsEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("flowLimits"));
        if (obj == null) {
            enable = false;
        } else {
            enable = obj.getBoolean("enable");
            rate = obj.getLong("rate");
            tokenBucket = new TokenBucket(obj.getInteger("maxSize"));
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public long getRate() {
        return rate;
    }

    public TokenBucket getTokenBucket() {
        return tokenBucket;
    }
}
