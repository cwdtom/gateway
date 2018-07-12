package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.constant.SystemConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * config environment
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class ConfigEnvironment {
    /**
     * config json map
     */
    private Map<String, String> config;
    /**
     * develop environment or not
     */
    private boolean isDevelop;

    /**
     * get config json string
     *
     * @param key key
     * @return json string
     */
    public String getChild(String key) {
        return config.get(key);
    }

    ConfigEnvironment(String json) {
        JSONObject obj = JSONObject.parseObject(json);
        Map<String, String> map = new HashMap<>(obj.size() / 3 * 4);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toString());
        }
        config = map;
        Object o = map.get("mode");
        this.isDevelop = o != null && o.toString().equals(SystemConstant.DEVELOP);
    }

    public boolean isDevelop() {
        return isDevelop;
    }
}
