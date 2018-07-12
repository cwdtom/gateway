package com.github.cwdtom.gateway.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.environment.ConfigEnvironment;
import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.environment.lb.RandomLoadBalance;
import com.github.cwdtom.gateway.environment.lb.UrlMapping;
import com.github.cwdtom.gateway.mapping.Mapper;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * application context utils
 *
 * @author chenweidong
 * @since 3.0.2
 */
public class ContextUtils {
    /**
     * build mapping environment
     *
     * @param config json config
     * @return mapping environment
     * @throws Exception build error
     */
    public static MappingEnvironment buildMappingEnvironment(ConfigEnvironment config) throws Exception {
        JSONObject mappingObj = JSON.parseObject(config.getChild("mapping"));
        JSONObject obj = mappingObj.getJSONObject("list");
        Map<String, List<Mapper>> map = new HashMap<>(obj.size() / 3 * 4);
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            JSONArray arr = (JSONArray) entry.getValue();
            int len = arr.size();
            List<Mapper> urls = new Vector<>(len);
            for (int i = 0; i < len; i++) {
                JSONObject object = arr.getJSONObject(i);
                urls.add(new Mapper(entry.getKey(), object.getString("url"), object.getInteger("weight")));
            }
            map.put(entry.getKey(), urls);
        }

        String className = mappingObj.getString("mode");
        if (className == null) {
            return new RandomLoadBalance(map);
        }
        Class<? extends UrlMapping> clazz = Class.forName(className).asSubclass(UrlMapping.class);
        Constructor<? extends UrlMapping> constructor = clazz.getDeclaredConstructor(Map.class);
        constructor.setAccessible(true);
        return constructor.newInstance(map);
    }
}
