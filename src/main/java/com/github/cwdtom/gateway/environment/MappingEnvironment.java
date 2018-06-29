package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.environment.impl.RandomLoadBalance;
import com.github.cwdtom.gateway.environment.impl.UrlMapping;
import com.github.cwdtom.gateway.mapping.Mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 映射配置
 *
 * @author chenweidong
 * @since 2.1.0
 */
public interface MappingEnvironment {
    /**
     * 获取映射表
     *
     * @return 映射表
     */
    Map<String, List<Mapper>> get();

    /**
     * 获取负载均衡映射对象
     *
     * @param host host
     * @param ip   ip
     * @return 映射对象
     */
    Mapper getLoadBalance(String host, String ip);

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
    static MappingEnvironment buildMappingEnvironment(ConfigEnvironment config)
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
                urls.add(new Mapper(object.getString("url"), object.getInteger("weight")));
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
