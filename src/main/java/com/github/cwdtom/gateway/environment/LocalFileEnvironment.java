package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * local file environment
 *
 * @author chenweidong
 * @since 3.1.1
 */
public class LocalFileEnvironment {
    /**
     * file path mapping map
     */
    private Map<String, String> pathMapping = new HashMap<>();

    LocalFileEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("static"));
        if (obj != null) {
            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                pathMapping.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * get local file path
     *
     * @param host host
     * @return file path
     */
    public String getPath(String host) {
        return pathMapping.get(host);
    }

    /**
     * get mapping map
     *
     * @return mapping map
     */
    public Map<String, String> get() {
        return pathMapping;
    }
}
