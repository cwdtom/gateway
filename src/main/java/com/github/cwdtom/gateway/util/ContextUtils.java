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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 上下文工具
 *
 * @author chenweidong
 * @since 3.0.2
 */
public class ContextUtils {
    /**
     * 生成映射环境
     *
     * @param config 配置
     * @return 映射环境
     * @throws ClassNotFoundException    class不存在
     * @throws NoSuchMethodException     方法不存在
     * @throws IllegalAccessException    没有调用权限
     * @throws InvocationTargetException 调用目标方法异常
     * @throws InstantiationException    初始化异常
     */
    public static MappingEnvironment buildMappingEnvironment(ConfigEnvironment config)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
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
