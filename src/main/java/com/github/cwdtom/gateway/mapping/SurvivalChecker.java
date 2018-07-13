package com.github.cwdtom.gateway.mapping;

import com.github.cwdtom.gateway.constant.HttpConstant;
import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.environment.ConsulEnvironment;
import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.environment.ZookeeperEnvironment;
import com.github.cwdtom.gateway.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * survival checker
 *
 * @author chenweidong
 * @since 1.4.0
 */
@Slf4j
public class SurvivalChecker implements Runnable {
    /**
     * error mapper list
     */
    private static Set<Mapper> mappers = new HashSet<>();
    /**
     * application context
     */
    private ApplicationContext context;

    public SurvivalChecker(ApplicationContext context) {
        this.context = context;
    }

    /**
     * add error mapper
     *
     * @param mapper error mapper
     */
    public static void add(Mapper mapper) {
        mappers.add(mapper);
    }

    @Override
    public void run() {
        ConsulEnvironment consul = context.getContext(ConsulEnvironment.class);
        ZookeeperEnvironment zk = context.getContext(ZookeeperEnvironment.class);
        int count = 0;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Iterator<Mapper> iterator = mappers.iterator();
                while (iterator.hasNext()) {
                    Mapper m = iterator.next();
                    try {
                        HttpUtils.sendGet(HttpConstant.HTTP_PREFIX + m.getTarget());
                        // restore mapper
                        m.restExceptionCount();
                        iterator.remove();
                    } catch (IOException ignored) {
                    }
                }
                Thread.sleep(10000);
                // rebuild all mapper each 100-second from zk or consul.
                count++;
                if (count % 10 == 0) {
                    if (consul.isEnable()) {
                        context.setContext(MappingEnvironment.class, consul.buildMapping());
                        mappers.clear();
                    }
                    if (zk.isEnable()) {
                        context.setContext(MappingEnvironment.class, zk.buildMapping());
                        mappers.clear();
                    }
                }
            }
        } catch (Exception e) {
            log.error("survival check service exception.", e);
            run();
        }
    }
}
