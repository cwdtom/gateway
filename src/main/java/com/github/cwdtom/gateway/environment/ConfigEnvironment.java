package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 配置环境
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class ConfigEnvironment {
    /**
     * 配置json对象
     */
    private static JSONObject object;

    /**
     * 获取配置json
     *
     * @return 配置json
     */
    public static JSONObject getChild(String key) {
        return object.getJSONObject(key);
    }

    /**
     * 初始化配置环境
     *
     * @param filePath 配置文件路径
     */
    public static void init(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            object = JSONObject.parseObject(content);
        } catch (IOException e) {
            log.error("config file is not found.");
            System.exit(1);
        }
    }
}
