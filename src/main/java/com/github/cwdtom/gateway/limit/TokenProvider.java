package com.github.cwdtom.gateway.limit;

import com.github.cwdtom.gateway.environment.ApplicationContext;
import com.github.cwdtom.gateway.environment.FlowLimitsEnvironment;
import lombok.extern.slf4j.Slf4j;

/**
 * 令牌生产者
 *
 * @author chenweidong
 * @since 1.3.0
 */
@Slf4j
public class TokenProvider implements Runnable {
    /**
     * 应用上下文
     */
    private final ApplicationContext applicationContext;

    public TokenProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        FlowLimitsEnvironment env = applicationContext.getContext(FlowLimitsEnvironment.class);
        if (env.isEnable()) {
            log.info("token start production.");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    env.getTokenBucket().offer();
                    Thread.sleep(env.getRate());
                }
            } catch (InterruptedException e) {
                log.error("token production exception.", e);
                // 重启
                run();
            }
        }
    }
}
