package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 静态文件配置
 *
 * @author chenweidong
 * @since 1.7.0
 */
public class StaticEnvironment {
    /**
     * 路径映射表
     */
    private Map<String, String> pathMapping = new HashMap<>();

    StaticEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("static"));
        if (obj != null) {
            for (Map.Entry<String, Object> entry : obj.entrySet()) {
                pathMapping.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * 获取静态文件映射路径
     *
     * @param host host
     * @return 路径
     */
    public String getPath(String host) {
        return pathMapping.get(host);
    }

    /**
     * 获取映射表
     *
     * @return 映射表
     */
    public Map<String, String> get() {
        return pathMapping;
    }
}
