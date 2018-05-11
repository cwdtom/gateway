package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.entity.Constant;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * https环境
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class HttpsEnvironment {
    /**
     * 单例
     */
    private static HttpsEnvironment instance;
    /**
     * 端口号
     */
    private int port;

    /**
     * 获取https环境对象
     *
     * @return https环境对象
     */
    public static HttpsEnvironment get() {
        return instance;
    }

    static {
        InputStream input = HttpEnvironment.class.getResourceAsStream(Constant.CONFIG_FILE_PATH);
        if (input == null) {
            log.error("application.yml is not found.");
            System.exit(1);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> object = yaml.load(input);
        // http key 对应的为map类
        @SuppressWarnings("unchecked")
        Map<String, Object> httpMap = (Map<String, Object>) object.get("https");
        HttpsEnvironment env = new HttpsEnvironment();
        env.port = (int) httpMap.get("port");
        HttpsEnvironment.instance = env;
    }

    public int getPort() {
        return port;
    }
}
