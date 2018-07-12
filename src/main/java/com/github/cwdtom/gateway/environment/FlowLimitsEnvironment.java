package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.limit.TokenBucket;

/**
 * flow limits
 *
 * @author chenweidong
 * @since 1.3.0
 */
public class FlowLimitsEnvironment {
    /**
     * enable
     */
    private boolean enable;
    /**
     * token provide rate
     */
    private long rate;
    /**
     * token bucket
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
