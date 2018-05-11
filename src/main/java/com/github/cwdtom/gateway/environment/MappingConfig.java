package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.entity.Constant;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 映射配置
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class MappingConfig {
    /**
     * 映射表
     */
    private static Map<String, String[]> urlMapping;
    /**
     * 随机对象
     */
    private static Random random = new Random();

    static {
        InputStream input = HttpEnvironment.class.getResourceAsStream(Constant.CONFIG_FILE_PATH);
        if (input == null) {
            log.error("application.yml is not found.");
            System.exit(1);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> object = yaml.load(input);
        // mapping key 对应的为map类
        @SuppressWarnings("unchecked")
        Map<String, String> mappingMap = (Map<String, String>) object.get("mapping");
        Map<String, String[]> map = new HashMap<>(mappingMap.size() / 3 * 4);
        for (Map.Entry<String, String> entry : mappingMap.entrySet()) {
            // 去除空格
            String urls = entry.getValue().replace(" ", "");
            map.put(entry.getKey(), urls.split(","));
        }
        urlMapping = map;
    }

    /**
     * 获取映射地址
     *
     * @param host 原地址
     * @return 映射地址
     */
    public static String getMappingIsostatic(String host) {
        String[] urls = urlMapping.get(host);
        if (urls == null) {
            return null;
        }
        int index = random.nextInt(urls.length);
        return urls[index];
    }
}
