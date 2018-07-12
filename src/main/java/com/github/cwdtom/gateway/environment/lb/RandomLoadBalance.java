package com.github.cwdtom.gateway.environment.lb;

import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * random load balance
 *
 * @author chenweidong
 * @since 2.1.0
 */
@Slf4j
public class RandomLoadBalance extends UrlMapping {
    /**
     * random seed
     */
    private Random random = new Random();

    public RandomLoadBalance(Map<String, List<Mapper>> urlMapping) {
        super(urlMapping);
        log.info("RandomLoadBalance load completed.");
    }

    @Override
    public Mapper getLoadBalance(String host, String ip) {
        List<Mapper> urls = super.mapping.get(host);
        if (urls == null || urls.size() == 0) {
            return null;
        }
        int sum = 0;
        for (Mapper m : urls) {
            if (m.isOnline()) {
                sum += m.getWeight();
            }
        }
        // not found
        if (sum == 0) {
            return null;
        }
        int target = random.nextInt(sum);
        sum = 0;
        for (Mapper m : urls) {
            if (m.isOnline()) {
                sum += m.getWeight();
                if (sum >= target) {
                    return m;
                }
            }
        }
        return null;
    }
}
