package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 映射配置
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class MappingConfig {
    /**
     * 映射表
     */
    private static Map<String, String[]> urlMapping;
    /**
     * 随机对象
     */
    private static Random random = new Random();

    static {
        JSONObject obj = ConfigEnvironment.getChild("mapping");
        Map<String, String[]> map = new HashMap<>(obj.size() / 3 * 4);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            JSONArray arr = (JSONArray) entry.getValue();
            int len = arr.size();
            String[] urls = new String[len];
            for (int i = 0; i < len; i++) {
                urls[i] = arr.getString(i);
            }
            map.put(entry.getKey(), urls);
        }
        urlMapping = map;
    }

    /**
     * 获取映射地址
     *
     * @param host 原地址
     * @return 映射地址
     */
    public static String getMappingIsostatic(String host) {
        String[] urls = urlMapping.get(host);
        if (urls == null) {
            return null;
        }
        int index = random.nextInt(urls.length);
        return urls[index];
    }
}
