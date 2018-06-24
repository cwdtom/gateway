package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.handler.RequestHandler;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池
 *
 * @author chenweidong
 * @since 1.0.0
 */
@Slf4j
public class ThreadPool {
    /**
     * 服务线程池
     */
    private static ThreadPoolExecutor serviceThreadPool;
    /**
     * 线程池组
     */
    private static Map<String, ThreadPoolExecutor> threadPoolMap;

    /**
     * 执行任务
     *
     * @param r 任务
     */
    public static void execute(Runnable r) {
        serviceThreadPool.execute(r);
    }

    /**
     * 执行线程组任务
     *
     * @param host host
     * @param r    任务
     */
    public static void execute(String host, RequestHandler r) {
        ThreadPoolExecutor executor = threadPoolMap.get(host);
        if (executor == null) {
            r.release(true);
            log.warn("{} is not found.", host);
            return;
        }
        executor.execute(r);
    }

    /**
     * 销毁线程池
     */
    public static void shutdown() {
        serviceThreadPool.shutdown();
        for (ThreadPoolExecutor t : threadPoolMap.values()) {
            t.shutdown();
        }
    }

    static {
        serviceThreadPool = new ThreadPoolExecutor(10, 10, 2000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10), new DefaultThreadFactory("service"));
        JSONObject obj = ConfigEnvironment.getChild("threadPool");
        int core = obj.getInteger("core");
        int max = obj.getInteger("max");
        int timeout = obj.getInteger("timeout");
        Map<String, List<Mapper>> mappingMap = MappingConfig.get();
        Map<String, String> pathMap = StaticConfig.get();
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

    /**
     * 配置默认线程工厂
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        /**
         * host
         */
        private String host;
        /**
         * 原子数
         */
        private AtomicInteger count = new AtomicInteger(0);

        DefaultThreadFactory(String host) {
            this.host = host;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "gateway-" + host + "-" + count.incrementAndGet());
        }
    }

    /**
     * 拒绝操作
     */
    private static class DefaultRejectedExecutionHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof RequestHandler) {
                ((RequestHandler) r).release(true);
            }
            log.warn("mission rejected.", executor);
        }
    }
}
