package com.github.cwdtom.gateway.environment;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
public final class ApplicationContext {
    /**
     * 配置
     */
    private final Map<Class, Object> context = new ConcurrentHashMap<>();

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
            System.out.println("config file is not found.");
            System.exit(1);
        }
        if (!config.isDevelop()) {
            // 如果不是开发者模式，提高日志等级
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = loggerContext.getLogger("root");
            logger.setLevel(Level.toLevel("WARN"));
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
        context.put(FilterEnvironment.class, new FilterEnvironment(config));
        ConsulEnvironment consul = new ConsulEnvironment(config);
        context.put(ConsulEnvironment.class, consul);
        if (consul.isEnable()) {
            context.put(MappingEnvironment.class, consul.buildMapping());
        }
    }

    /**
     * 获取上下文
     *
     * @param clazz 配置类
     * @param <T>   类泛型
     * @return 配置对象
     */
    public <T> T getContext(Class<T> clazz) {
        return clazz.cast(context.get(clazz));
    }

    /**
     * 设置上下文
     *
     * @param clazz 配置类
     * @param t     实体对象
     * @param <T>   类泛型
     */
    public <T> void setContext(Class<T> clazz, T t) {
        context.put(clazz, t);
    }
}
