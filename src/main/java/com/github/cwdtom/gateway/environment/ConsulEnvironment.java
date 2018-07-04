package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import com.github.cwdtom.gateway.environment.lb.ConsistentHash;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * consul配置
 *
 * @author chenweidong
 * @since 3.0.0
 */
@Slf4j
public class ConsulEnvironment {
    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * consul客户端
     */
    private ConsulClient client;
    /**
     * server映射host
     */
    private Map<String, List<String>> map = new HashMap<>();

    ConsulEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("consul"));
        enable = obj.getBoolean("enable");
        client = new ConsulClient(obj.getString("host"));
        JSONObject mapping = obj.getJSONObject("mapping");
        for (Map.Entry<String, Object> entry : mapping.entrySet()) {
            JSONArray arr = (JSONArray) entry.getValue();
            map.put(entry.getKey(), arr.toJavaList(String.class));
        }
    }

    public boolean isEnable() {
        return enable;
    }

    /**
     * 构造负载均衡
     *
     * @return 映射接口
     */
    public MappingEnvironment buildMapping() {
        log.info("consul service discovery, rebuild load balance.");
        Response<List<HealthService>> response = client.getHealthServices("test",
                true, QueryParams.DEFAULT);
        Map<String, List<Mapper>> mapping = new HashMap<>(16);
        for (HealthService h : response.getValue()) {
            HealthService.Service service = h.getService();
            List<String> hosts = map.get(service.getService());
            if (hosts != null) {
                for (String s : hosts) {
                    List<Mapper> list = mapping.computeIfAbsent(s, k -> new Vector<>());
                    list.add(new Mapper(s, service.getAddress() + ":" + service.getPort(), 100));
                }
            }
        }
        // 使用一致性hash负载
        return new ConsistentHash(mapping);
    }
}
