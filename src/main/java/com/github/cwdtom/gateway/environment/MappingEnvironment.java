package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.mapping.Mapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 映射配置
 *
 * @author chenweidong
 * @since 1.0.0
 */
public class MappingEnvironment {
    /**
     * 映射表
     */
    private Map<String, List<Mapper>> urlMapping;
    /**
     * 随机数对象
     */
    private Random random = new Random();

    MappingEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("mapping"));
        Map<String, List<Mapper>> map = new ConcurrentHashMap<>(obj.size() / 3 * 4);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            JSONArray arr = (JSONArray) entry.getValue();
            int len = arr.size();
            List<Mapper> urls = new Vector<>(len);
            for (int i = 0; i < len; i++) {
                JSONObject object = arr.getJSONObject(i);
                urls.add(new Mapper(object.getString("url"), object.getInteger("weight")));
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
    public Map<String, List<Mapper>> get() {
        return urlMapping;
    }

    /**
     * 获取映射地址
     * RandomLoadBalance 随机负载均衡算法
     *
     * @param host 原地址
     * @return 映射地址
     */
    public Mapper getRandomLoadBalance(String host) {
        List<Mapper> urls = urlMapping.get(host);
        if (urls == null || urls.size() == 0) {
            return null;
        }
        int sum = 0;
        for (Mapper m : urls) {
            if (m.isOnline()) {
                sum += m.getWeight();
            }
        }
        // 无可用代理对象
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
