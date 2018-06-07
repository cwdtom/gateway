package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.mapping.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 映射配置
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class MappingConfig {
    /**
     * 映射表
     */
    private static Map<String, List<Mapper>> urlMapping;

    static {
        JSONObject obj = ConfigEnvironment.getChild("mapping");
        Map<String, List<Mapper>> map = new ConcurrentHashMap<>(obj.size() / 3 * 4);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            JSONArray arr = (JSONArray) entry.getValue();
            int len = arr.size();
            List<Mapper> urls = new Vector<>(len);
            for (int i = 0; i < len; i++) {
                urls.add(new Mapper(arr.getString(i)));
            }
            map.put(entry.getKey(), urls);
        }
        urlMapping = map;
    }

    /**
     * 获取mapping
     *
     * @return mapping
     */
    public static Map<String, List<Mapper>> get() {
        return urlMapping;
    }

    /**
     * 获取映射地址
     *
     * @param host 原地址
     * @return 映射地址
     */
    public static Mapper getMappingIsostatic(String host) {
        List<Mapper> urls = urlMapping.get(host);
        if (urls == null || urls.size() == 0) {
            return null;
        }
        Mapper mapper = null;
        for (Mapper m : urls) {
            if (mapper == null) {
                if (m.isOnline()) {
                    mapper = m;
                }
            } else {
                if (mapper.compareTo(m) < 0) {
                    mapper = m;
                }
            }
        }
        return mapper;
    }
}
