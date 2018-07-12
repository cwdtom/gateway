package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.environment.lb.UrlMapping;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * zookeeper environment
 *
 * @author chenweidong
 * @since 3.1.0
 */
@Slf4j
public class ZookeeperEnvironment {
    /**
     * enable
     */
    private boolean enable;
    /**
     * zk client
     */
    private CuratorFramework client;
    /**
     * server mapping host map
     */
    private Map<String, List<String>> map = new HashMap<>();
    /**
     * algorithms class
     */
    private Class<? extends UrlMapping> clazz;

    ZookeeperEnvironment(ConfigEnvironment config) throws ClassNotFoundException {
        JSONObject obj = JSON.parseObject(config.getChild("zk"));
        if (obj == null) {
            enable = false;
        } else {
            enable = obj.getBoolean("enable");
            if (enable) {
                client = CuratorFrameworkFactory.newClient(obj.getString("host"),
                        new RetryNTimes(10, 5000));
                client.start();
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
        log.info("zookeeper service discovery, rebuild load balance.");
        List<String> children = client.getChildren().forPath("/services");

        Map<String, List<Mapper>> mapping = new HashMap<>(16);
        for (String s : children) {
            List<String> nodes = client.getChildren().forPath("/services/" + s);
            for (String n : nodes) {
                byte[] bytes = client.getData().forPath("/services/" + s + "/" + n);
                JSONObject obj = JSON.parseObject(new String(bytes));
                List<String> hosts = map.get(s);
                if (hosts != null) {
                    for (String h : hosts) {
                        List<Mapper> list = mapping.computeIfAbsent(h, k -> new Vector<>());
                        list.add(new Mapper(s, obj.getString("address") + ":" + obj.getString("port"),
                                100));
                    }
                }
            }
        }

        Constructor<? extends UrlMapping> constructor = clazz.getDeclaredConstructor(Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(mapping);
    }
}
