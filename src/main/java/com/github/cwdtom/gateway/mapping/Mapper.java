package com.github.cwdtom.gateway.mapping;

import io.netty.handler.codec.http.HttpMethod;

/**
 * 映射器
 *
 * @author chenweidong
 * @since 1.4.0
 */
public class Mapper implements Comparable<Mapper> {
    /**
     * 调用次数
     */
    private int count;
    /**
     * 异常次数
     */
    int exceptionCount;
    /**
     * 目标url
     */
    String target;

    public Mapper(String target) {
        this.target = target;
        this.count = 0;
        this.exceptionCount = 0;
    }

    public String getTarget() {
        this.count++;
        return target;
    }

    /**
     * 释放
     */
    public void release() {
        this.count--;
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
        return exceptionCount < 3;
    }

    @Override
    public int compareTo(Mapper o) {
        if (!this.isOnline()) {
            return 1;
        }
        return Integer.compare(o.count, this.count);
    }
}
