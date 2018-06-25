package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置环境
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class ConfigEnvironment {
    /**
     * 配置json对象
     */
    private Map<String, String> config;

    /**
     * 获取配置json
     *
     * @return json string
     */
    public String getChild(String key) {
        return config.get(key);
    }

    /**
     * 创建配置文件环境
     *
     * @param json json string
     */
    ConfigEnvironment(String json) {
        JSONObject obj = JSONObject.parseObject(json);
        Map<String, String> map = new HashMap<>(obj.size() / 3 * 4);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toString());
        }
        config = map;
    }
}
