package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.mapping.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 映射配置
 *
 * @author chenweidong
 * @since 2.1.0
 */
public interface MappingEnvironment {
    /**
     * 获取映射表
     *
     * @return 映射表
     */
    Map<String, List<Mapper>> get();

    /**
     * 获取负载均衡映射对象
     *
     * @param host host
     * @param ip   ip
     * @return 映射对象
     */
    Mapper getLoadBalance(String host, String ip);
}
