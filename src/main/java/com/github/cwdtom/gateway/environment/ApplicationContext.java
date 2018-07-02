package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<Class, Object> context = new HashMap<>();

    public ApplicationContext(String filePath) throws ClassNotFoundException, InvocationTargetException,
            NoSuchMethodException, InstantiationException, IllegalAccessException {
        // 关闭内存泄漏检测
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        ConfigEnvironment config = null;
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            config = new ConfigEnvironment(json);
            context.put(ConfigEnvironment.class, config);
        } catch (IOException e) {
            log.error("config file is not found.");
            System.exit(1);
        }
        context.put(CorsEnvironment.class, new CorsEnvironment(config));
        context.put(FlowLimitsEnvironment.class, new FlowLimitsEnvironment(config));
        context.put(HttpEnvironment.class, new HttpEnvironment(config));
        context.put(HttpsEnvironment.class, new HttpsEnvironment(config));
        MappingEnvironment mappingEnv = MappingEnvironment.buildMappingEnvironment(config);
        context.put(MappingEnvironment.class, mappingEnv);
        StaticEnvironment staticEnv = new StaticEnvironment(config);
        context.put(StaticEnvironment.class, staticEnv);
        context.put(ThreadPoolGroup.class, new ThreadPoolGroup(config, mappingEnv, staticEnv));
    }

    /**
     * 获取上下文
     *
     * @param clazz 配置类
     * @return 配置对象
     */
    public <T> T getContext(Class<T> clazz) {
        return clazz.cast(context.get(clazz));
    }
}
