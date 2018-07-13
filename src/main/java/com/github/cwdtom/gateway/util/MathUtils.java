package com.github.cwdtom.gateway.util;

/**
 * math utils
 *
 * @author chenweidong
 * @since 3.1.2
 */
public class MathUtils {
    /**
     * max common divisor
     *
     * @param x x
     * @param y y
     * @return result
     */
    public static int maxCommonDivisor(int x, int y) {
        int max, min, tmp;
        max = x > y ? x : y;
        min = x < y ? x : y;
        while (max != min)
        {
            tmp = max - min;
            max = (tmp > min) ? tmp : min;
            min = (tmp < min) ? tmp : min;
        }
        return max;
    }
}
