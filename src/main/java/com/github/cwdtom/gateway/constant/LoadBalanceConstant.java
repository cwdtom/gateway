package com.github.cwdtom.gateway.constant;

/**
 * load balance constant
 *
 * @author chenweidong
 * @since 3.0.2
 */
public class LoadBalanceConstant {
    /**
     * when trigger 3 exceptions,take this mapper off
     */
    public static final int OFFLINE_COUNT = 3;
    /**
     * node max count
     */
    public static final int MAX_NODE_SIZE = 150;
    /**
     * middle number in integer
     */
    public static final int MID_INT = Integer.MAX_VALUE / 2;
}
