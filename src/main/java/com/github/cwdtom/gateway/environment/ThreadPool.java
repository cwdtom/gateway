package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.mapping.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池
 *
 * @author chenweidong
 * @since 1.0.0
 */
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
    public static void execute(String host, Runnable r) {
        threadPoolMap.get(host).execute(r);
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
        Map<String, List<Mapper>> map = MappingConfig.get();
        threadPoolMap = new HashMap<>(map.size() / 3 * 4);
        for (String key : map.keySet()) {
            threadPoolMap.put(key, new ThreadPoolExecutor(core, max, timeout, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(core >> 1), new DefaultThreadFactory(key)));
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
}
