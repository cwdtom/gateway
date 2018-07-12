package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import com.github.cwdtom.gateway.environment.lb.UrlMapping;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * consul config
 *
 * @author chenweidong
 * @since 3.0.0
 */
@Slf4j
public class ConsulEnvironment {
    /**
     * enable
     */
    private boolean enable;
    /**
     * consul client
     */
    private ConsulClient client;
    /**
     * server mapping host
     */
    private Map<String, List<String>> map = new HashMap<>();
    /**
     * algorithms class
     */
    private Class<? extends UrlMapping> clazz;

    ConsulEnvironment(ConfigEnvironment config) throws ClassNotFoundException {
        JSONObject obj = JSON.parseObject(config.getChild("consul"));
        if (obj == null) {
            enable = false;
        } else {
            enable = obj.getBoolean("enable");
            if (enable) {
                client = new ConsulClient(obj.getString("host"));
                JSONObject mapping = obj.getJSONObject("mapping");
                for (Map.Entry<String, Object> entry : mapping.entrySet()) {
                    JSONArray arr = (JSONArray) entry.getValue();
                    map.put(entry.getKey(), arr.toJavaList(String.class));
                }
                JSONObject mappingObj = JSON.parseObject(config.getChild("mapping"));
                clazz = Class.forName(mappingObj.getString("mode")).asSubclass(UrlMapping.class);
            }
        }
    }

    public boolean isEnable() {
        return enable;
    }

    /**
     * build load balance
     *
     * @return mapping environment
     * @throws Exception build error
     */
    public MappingEnvironment buildMapping() throws Exception {
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
        Constructor<? extends UrlMapping> constructor = clazz.getDeclaredConstructor(Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(mapping);
    }
}
