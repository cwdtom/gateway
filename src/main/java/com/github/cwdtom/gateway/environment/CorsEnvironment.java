package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Set;
import java.util.TreeSet;

/**
 * cors config
 *
 * @author chenweidong
 * @since 1.2.0
 */
public class CorsEnvironment {
    /**
     * enable
     */
    private boolean enable;
    /**
     * white list
     */
    private Set<String> whiteList;
    /**
     * allow methods
     */
    private String allowMethods;

    CorsEnvironment(ConfigEnvironment config) {
        JSONObject obj = JSON.parseObject(config.getChild("cors"));
        if (obj == null) {
            enable = false;
        } else {
            enable = obj.getBoolean("enable");
            allowMethods = obj.getString("allowMethods");
            JSONArray array = obj.getJSONArray("whiteList");
            Set<String> whiteList = new TreeSet<>();
            for (int i = 0; i < array.size(); i++) {
                whiteList.add(array.getString(i));
            }
            this.whiteList = whiteList;
        }
    }

    /**
     * check for origin legal
     *
     * @param origin origin host
     * @return legal or not
     */
    public boolean isLegal(String origin) {
        return enable && (whiteList.size() <= 0 || whiteList.contains(origin));
    }

    public String getAllowMethods() {
        return allowMethods;
    }
}
