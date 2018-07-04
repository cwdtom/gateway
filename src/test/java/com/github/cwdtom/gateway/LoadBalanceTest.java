package com.github.cwdtom.gateway;

import com.github.cwdtom.gateway.environment.lb.ConsistentHash;
import com.github.cwdtom.gateway.environment.lb.RandomLoadBalance;
import com.github.cwdtom.gateway.mapping.Mapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡测试
 *
 * @author chenweidong
 */
public class LoadBalanceTest {
    /**
     * 一致性hash算法
     */
    private ConsistentHash consistentHash;
    /**
     * 随机权重算法
     */
    private RandomLoadBalance randomLoadBalance;

    @Before
    public void setup() {
        Map<String, List<Mapper>> urlMapping = new HashMap<>();
        urlMapping.put("127.0.0.1:8080", Arrays.asList(
                new Mapper("127.0.0.1:8080","123.125.115.110:80", 200),
                new Mapper("127.0.0.1:8080","220.181.57.216:80", 100)));
        consistentHash = new ConsistentHash(urlMapping);
        randomLoadBalance = new RandomLoadBalance(urlMapping);

    }

    @Test
    public void testConsistentHash() {
        Assert.assertEquals("123.125.115.110:80",
                consistentHash.getLoadBalance("127.0.0.1:8080", "::0").getTarget());
    }

    @Test
    public void testRandomLoadBalance() {
        Assert.assertNotNull(randomLoadBalance.getLoadBalance("127.0.0.1:8080", "::0"));
    }
}
