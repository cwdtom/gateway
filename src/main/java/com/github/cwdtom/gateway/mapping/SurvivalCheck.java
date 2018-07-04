package com.github.cwdtom.gateway.mapping;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.constant.HttpConstant;
import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.environment.ConsulEnvironment;
import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 存活检查
 *
 * @author chenweidong
 * @since 1.4.0
 */
@Slf4j
public class SurvivalCheck implements Runnable {
    /**
     * 熔断mapper列表
     */
    private static Set<Mapper> mappers = new HashSet<>();
    /**
     * 应用上下文
     */
    private ApplicationContext context;

    public SurvivalCheck(ApplicationContext context) {
        this.context = context;
    }

    /**
     * 添加已熔断mapper
     *
     * @param mapper mapper
     */
    public static void add(Mapper mapper) {
        mappers.add(mapper);
    }

    @Override
    public void run() {
        ConsulEnvironment consul = context.getContext(ConsulEnvironment.class);
        int count = 0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Iterator<Mapper> iterator = mappers.iterator();
                while (iterator.hasNext()) {
                    Mapper m = iterator.next();
                    try {
                        HttpUtils.sendGet(HttpConstant.HTTP_PREFIX + m.getTarget());
                        // 服务恢复
                        m.setExceptionCount(0);
                        iterator.remove();
                    } catch (IOException ignored) {
                    }
                }
                Thread.sleep(10000);
                // 每隔100秒从consul重建映射
                count++;
                if (count % 10 == 0 && consul.isEnable()) {
                    context.setContext(MappingEnvironment.class, consul.buildMapping());
                    mappers.clear();
                }
            }
        } catch (Exception e) {
            log.error("survival check service exception.", e);
            run();
        }
    }
}
