package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.entity.RequestTask;
import com.github.cwdtom.gateway.handler.RequestHandler;
import lombok.extern.slf4j.Slf4j;

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
     * 线程池单例
     */
    private static ThreadPoolExecutor threadPoolExecutor;
    /**
     * 线程数量
     */
    private static int core;

    /**
     * 执行任务
     */
    public static void startTask(BlockingQueue<RequestTask> queue) {
        for (int i = 0; i < core; i++) {
            threadPoolExecutor.execute(new RequestHandler(queue));
        }
    }

    static {
        JSONObject obj = ConfigEnvironment.getChild("threadPool");
        core = obj.getInteger("core");
        threadPoolExecutor = new ThreadPoolExecutor(core, core, 5000, TimeUnit.MILLISECONDS,
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
            return new Thread(r, "gateway-" + count.incrementAndGet());
        }
    }
}
