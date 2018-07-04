package com.github.cwdtom.gateway.mapping;

import com.github.cwdtom.gateway.constant.Constant;
import io.netty.handler.codec.http.HttpMethod;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 映射器
 *
 * @author chenweidong
 * @since 1.4.0
 */
@ToString
@Slf4j
public class Mapper {
    /**
     * 权重
     */
    private int weight;
    /**
     * 异常次数
     */
    private int exceptionCount;

    /**
     * 目标url
     */
    private String target;

    public Mapper(String target, Integer weight) {
        this.target = target;
        // 无权重时默认值100
        this.weight = weight == null || weight < 0 ? 100 : weight;
        this.exceptionCount = 0;
    }

    public String getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    /**
     * 目标代理异常
     *
     * @param method      请求类型
     * @param uri         uri
     * @param contentType 消息类型
     * @return 异常目标url
     */
    public String exception(HttpMethod method, String uri, String contentType) {
        this.exceptionCount++;
        if (!isOnline()) {
            log.error("{} offline.", target);
            // 放入存活检查列条
            SurvivalCheck.add(new OfflineMapper(this, method, uri, contentType));
        }
        return target;
    }

    /**
     * 检查是否健康
     *
     * @return 是否健康
     */
    public boolean isOnline() {
        return exceptionCount < Constant.OFFLINE_COUNT;
    }
}
