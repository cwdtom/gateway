package com.github.cwdtom.gateway.environment;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.github.cwdtom.gateway.thread.ThreadPoolGroup;
import com.github.cwdtom.gateway.util.ContextUtils;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * application context
 *
 * @author chenweidong
 * @since 1.7.2
 */
public final class ApplicationContext {
    /**
     * context
     */
    private final Map<Class, Object> context = new ConcurrentHashMap<>();

    public ApplicationContext(String filePath) throws Exception {
        ConfigEnvironment config;
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            config = new ConfigEnvironment(json);
            context.put(ConfigEnvironment.class, config);
        } catch (IOException e) {
            throw new FileNotFoundException("config file is not found.");
        }
        if (!config.isDevelop()) {
            // raise log level to 'warn' when it is not develop environment.
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = loggerContext.getLogger("root");
            logger.setLevel(Level.toLevel("WARN"));
            // shutdown resource leak detector,improve performance
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }
        context.put(CorsEnvironment.class, new CorsEnvironment(config));
        context.put(FlowLimitsEnvironment.class, new FlowLimitsEnvironment(config));
        context.put(HttpEnvironment.class, new HttpEnvironment(config));
        context.put(HttpsEnvironment.class, new HttpsEnvironment(config));
        MappingEnvironment mappingEnv = ContextUtils.buildMappingEnvironment(config);
        context.put(MappingEnvironment.class, mappingEnv);
        LocalFileEnvironment staticEnv = new LocalFileEnvironment(config);
        context.put(LocalFileEnvironment.class, staticEnv);
        context.put(ThreadPoolGroup.class, new ThreadPoolGroup(config, mappingEnv, staticEnv));
        context.put(FilterEnvironment.class, new FilterEnvironment(config));
        ConsulEnvironment consul = new ConsulEnvironment(config);
        context.put(ConsulEnvironment.class, consul);
        if (consul.isEnable()) {
            // rebuild mapping
            context.put(MappingEnvironment.class, consul.buildMapping());
        }
        ZookeeperEnvironment zk = new ZookeeperEnvironment(config);
        context.put(ZookeeperEnvironment.class, zk);
        if (zk.isEnable()) {
            // rebuild mapping
            context.put(MappingEnvironment.class, zk.buildMapping());
        }
    }

    /**
     * get context
     *
     * @param clazz context class
     * @param <T>   context type
     * @return context
     */
    public <T> T getContext(Class<T> clazz) {
        return clazz.cast(context.get(clazz));
    }

    /**
     * set context
     *
     * @param clazz context class
     * @param t     context type
     * @param <T>   context
     */
    public <T> void setContext(Class<T> clazz, T t) {
        context.put(clazz, t);
    }
}
