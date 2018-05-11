package com.github.cwdtom.gateway.environment;

import com.github.cwdtom.gateway.entity.Constant;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
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
        InputStream input = HttpEnvironment.class.getResourceAsStream(Constant.CONFIG_FILE_PATH);
        if (input == null) {
            log.error("application.yml is not found.");
            System.exit(1);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> object = yaml.load(input);
        // thread key 对应的为map类
        @SuppressWarnings("unchecked")
        Map<String, Object> threadMap = (Map<String, Object>) object.get("thread");
        // pool key 对应的为map类
        @SuppressWarnings("unchecked")
        Map<String, Object> poolMap = (Map<String, Object>) threadMap.get("pool");

        int min = (int) poolMap.get("min");
        int max = (int) poolMap.get("max");
        int timeout = (int) poolMap.get("timeout");
        threadPoolExecutor = new ThreadPoolExecutor(min, max, timeout, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(min >> 1), new DefaultThreadFactory());
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
