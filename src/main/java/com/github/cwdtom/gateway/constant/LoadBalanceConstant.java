package com.github.cwdtom.gateway.constant;

/**
 * 负载均衡常量
 *
 * @author chenweidong
 * @since 3.0.2
 */
public class LoadBalanceConstant {
    /**
     * 熔断临界错误次数
     */
    public static final int OFFLINE_COUNT = 3;
    /**
     * 节点最大数
     */
    public static final int MAX_NODE_SIZE = 150;
    /**
     * 中位数
     */
    public static final int MID_INT = Integer.MAX_VALUE / 2;
}
