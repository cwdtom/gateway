package com.github.cwdtom.gateway.environment;

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
     * 单例
     */
    private static CorsEnvironment instance;
    /**
     * 是否启用
     */
    private boolean enable;
    /**
     * 白名单
     */
    private Set<String> whiteList;

    static {
        CorsEnvironment env = new CorsEnvironment();
        JSONObject obj = ConfigEnvironment.getChild("cors");
        env.enable = obj.getBoolean("enable");
        JSONArray array = obj.getJSONArray("whiteList");
        Set<String> whiteList = new TreeSet<>();
        for (int i = 0; i < array.size(); i++) {
            whiteList.add(array.getString(i));
        }
        env.whiteList = whiteList;
        CorsEnvironment.instance = env;
    }

    /**
     * origin是否命中白名单
     *
     * @param origin origin
     * @return 是否命中
     */
    public static boolean isLegal(String origin) {
        return CorsEnvironment.instance.enable
                && (CorsEnvironment.instance.whiteList.size() <= 0
                || CorsEnvironment.instance.whiteList.contains(origin));
    }
}
