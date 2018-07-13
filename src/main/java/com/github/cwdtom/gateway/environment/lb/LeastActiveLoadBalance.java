package com.github.cwdtom.gateway.environment.lb;

import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * least active load balance
 *
 * @author chenweidong
 * @since 3.1.2
 */
@Slf4j
public class LeastActiveLoadBalance extends UrlMapping {
    /**
     * active map
     */
    private Map<String, Map<Mapper, AtomicInteger>> activeMap = new HashMap<>();

    public LeastActiveLoadBalance(Map<String, List<Mapper>> mapping) {
        super(mapping);
        for (Map.Entry<String, List<Mapper>> entry : mapping.entrySet()) {
            Map<Mapper, AtomicInteger> value = new HashMap<>();
            for (Mapper m : entry.getValue()) {
                value.put(m, new AtomicInteger(0));
            }
            activeMap.put(entry.getKey(), value);
        }
        log.info("LeastActiveLoadBalance load completed.");
    }

    @Override
    public Mapper getLoadBalance(String host, String ip) {
        List<Mapper> list = super.mapping.get(host);
        if (list == null) {
            return null;
        }
        Map<Mapper, AtomicInteger> value = activeMap.get(host);
        Mapper mapper = null;
        int min = Integer.MAX_VALUE;
        for (Mapper m : list) {
            if (m.isOnline()) {
                int tmp = value.get(m).get() / m.getWeight();
                if (min >= tmp) {
                    min = tmp;
                    mapper = m;
                }
            }
        }
        if (mapper != null) {
            value.get(mapper).incrementAndGet();
        }
        return mapper;
    }
}
