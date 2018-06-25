package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用上下文
 *
 * @author chenweidong
 * @since 1.7.2
 */
@Slf4j
public final class ApplicationContext {
    /**
     * 配置
     */
    private final static Map<Class, Object> CONTEXT = new ConcurrentHashMap<>();

    public ApplicationContext(String filePath) {
        // 关闭内存泄漏检测
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        ConfigEnvironment config = null;
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            config = new ConfigEnvironment(json);
            CONTEXT.put(ConfigEnvironment.class, config);
        } catch (IOException e) {
            log.error("config file is not found.");
            System.exit(1);
        }
        CONTEXT.put(CorsEnvironment.class, new CorsEnvironment(config));
        CONTEXT.put(FlowLimitsEnvironment.class, new FlowLimitsEnvironment(config));
        CONTEXT.put(HttpEnvironment.class, new HttpEnvironment(config));
        CONTEXT.put(HttpsEnvironment.class, new HttpsEnvironment(config));
        MappingEnvironment mappingEnv = new MappingEnvironment(config);
        CONTEXT.put(MappingEnvironment.class, mappingEnv);
        StaticEnvironment staticEnv = new StaticEnvironment(config);
        CONTEXT.put(StaticEnvironment.class, staticEnv);
        CONTEXT.put(ThreadPoolGroup.class, new ThreadPoolGroup(config, mappingEnv, staticEnv));
    }

    /**
     * 获取上下文
     *
     * @param clazz 配置类
     * @return 配置对象
     */
    public <T> T getContext(Class<T> clazz) {
        return clazz.cast(CONTEXT.get(clazz));
    }
}
