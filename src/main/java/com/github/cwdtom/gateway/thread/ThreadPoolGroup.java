package com.github.cwdtom.gateway.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.environment.ConfigEnvironment;
import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.environment.LocalFileEnvironment;
import com.github.cwdtom.gateway.handler.RequestHandler;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * thread pool group management
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class ThreadPoolGroup {
    /**
     * thread pool group
     */
    private Map<String, ThreadPoolExecutor> threadPoolMap;

    /**
     * execute mission in mapping thread pool
     *
     * @param host host
     * @param r    mission
     */
    public void execute(String host, RequestHandler r) {
        ThreadPoolExecutor executor = threadPoolMap.get(host);
        if (executor == null) {
            r.release(true);
            log.warn("{} IS NOT FOUND.", host);
            return;
        }
        executor.execute(r);
    }

    /**
     * shutdown thread pool
     */
    public void shutdown() {
        for (ThreadPoolExecutor t : threadPoolMap.values()) {
            t.shutdown();
        }
    }

    public ThreadPoolGroup(ConfigEnvironment config, MappingEnvironment mappingEnv, LocalFileEnvironment staticEnv) {
        JSONObject obj = JSON.parseObject(config.getChild("threadPool"));
        int core = obj.getInteger("core");
        int max = obj.getInteger("max");
        int timeout = obj.getInteger("timeout");
        Map<String, List<Mapper>> mappingMap = mappingEnv.get();
        Map<String, String> pathMap = staticEnv.get();
        threadPoolMap = new HashMap<>((mappingMap.size() + pathMap.size()) / 3 * 4);
        for (String key : mappingMap.keySet()) {
            threadPoolMap.put(key, new ThreadPoolExecutor(core, max, timeout, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(core >> 1), new DefaultThreadFactory(key),
                    new DefaultRejectedExecutionHandler()));
        }
        for (String key : pathMap.keySet()) {
            if (!threadPoolMap.containsKey(key)) {
                threadPoolMap.put(key, new ThreadPoolExecutor(core, max, timeout, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(core >> 1), new DefaultThreadFactory(key),
                        new DefaultRejectedExecutionHandler()));
            }
        }
    }
}
