package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.mapping.Mapper;

import java.util.List;
import java.util.Map;

/**
 * mapping environment
 *
 * @author chenweidong
 * @since 2.1.0
 */
public interface MappingEnvironment {
    /**
     * get mapping map
     *
     * @return mapping map
     */
    Map<String, List<Mapper>> get();

    /**
     * get proxy mapper by load balance
     *
     * @param host host
     * @param ip   ip
     * @return mapper
     */
    Mapper getLoadBalance(String host, String ip);
}
