package com.github.cwdtom.gateway.environment.impl;

import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * url映射对象
 *
 * @author chenweidong
 * @since 2.1.0
 */
@Slf4j
public class UrlMapping implements MappingEnvironment {
    /**
     * 映射表
     */
    Map<String, List<Mapper>> mapping;

    UrlMapping(Map<String, List<Mapper>> mapping) {
        this.mapping = mapping;
    }

    @Override
    public Map<String, List<Mapper>> get() {
        return mapping;
    }

    /**
     * 默认方法需要重写
     */
    @Override
    public Mapper getLoadBalance(String host, String ip) {
        log.warn("USING THE METHOD IS NOT RECOMMENDED, PLEASE OVERRIDE THIS METHOD.");
        List<Mapper> list = mapping.get(host);
        int index = (int) System.currentTimeMillis() % list.size();
        return list.get(index);
    }
}
