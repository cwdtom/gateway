package com.github.cwdtom.gateway.environment.lb;

import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * url mapping
 *
 * @author chenweidong
 * @since 2.1.0
 */
@Slf4j
public class UrlMapping implements MappingEnvironment {
    /**
     * mapping map
     */
    protected Map<String, List<Mapper>> mapping;

    UrlMapping(Map<String, List<Mapper>> mapping) {
        this.mapping = mapping;
    }

    @Override
    public final Map<String, List<Mapper>> get() {
        return mapping;
    }

    /**
     * default method,need override
     */
    @Override
    public Mapper getLoadBalance(String host, String ip) {
        log.warn("USING THE METHOD IS NOT RECOMMENDED, PLEASE OVERRIDE THIS METHOD.");
        List<Mapper> list = mapping.get(host);
        int index = (int) System.currentTimeMillis() % list.size();
        return list.get(index);
    }
}
