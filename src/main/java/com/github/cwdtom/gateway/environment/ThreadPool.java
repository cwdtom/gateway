package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class ThreadPool {
    /**
     * 线程池单例
     */
    private static ThreadPoolExecutor threadPoolExecutor;

    /**
     * 执行任务
     */
    public static void execute(Runnable r) {
        threadPoolExecutor.execute(r);
    }

    static {
        JSONObject obj = ConfigEnvironment.getChild("threadPool");
        int core = obj.getInteger("core");
        int max = obj.getInteger("max");
        int timeout = obj.getInteger("timeout");
        threadPoolExecutor = new ThreadPoolExecutor(core, max, timeout, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(core >> 1), new DefaultThreadFactory());
    }

    /**
     * 配置默认线程工厂
     */
    private static class DefaultThreadFactory implements ThreadFactory {
        /**
         * 原子数
         */
        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "gateway-" + count.toString());
        }
    }
}
