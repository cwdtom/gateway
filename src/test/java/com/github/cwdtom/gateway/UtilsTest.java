package com.github.cwdtom.gateway;

import com.github.cwdtom.gateway.util.MathUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * util test
 *
 * @author chenweidong
 */
public class UtilsTest {
    @Test
    public void maxCommonDivisorTest() {
        Assert.assertEquals(5, MathUtils.maxCommonDivisor(15, 20));
    }
}
