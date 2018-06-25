package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Set;
import java.util.TreeSet;

/**
 * cors环境配置
 *
 * @author chenweidong
 * @since 1.2.0
 */
public class CorsEnvironment {
    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * 白名单
     */
    private Set<String> whiteList;

    CorsEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("cors"));
        if (obj == null) {
            enable = false;
        } else {
            enable = obj.getBoolean("enable");
            JSONArray array = obj.getJSONArray("whiteList");
            Set<String> whiteList = new TreeSet<>();
            for (int i = 0; i < array.size(); i++) {
                whiteList.add(array.getString(i));
            }
            this.whiteList = whiteList;
        }
    }

    /**
     * origin是否命中白名单
     *
     * @param origin origin
     * @return 是否命中
     */
    public boolean isLegal(String origin) {
        return enable && (whiteList.size() <= 0 || whiteList.contains(origin));
    }
}
