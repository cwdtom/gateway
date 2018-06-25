package com.github.cwdtom.gateway.limit;

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
     * 是否中断
     */
    private boolean interrupted = false;

    @Override
    public void run() {
        FlowLimitsEnvironment env = FlowLimitsEnvironment.get();
        if (env.isEnable()) {
            log.error("token start production.");
            try {
                while (true) {
                    TokenPool.offer();
                    Thread.sleep(env.getRate());
                    if (interrupted) {
                        return;
                    }
                }
            } catch (InterruptedException e) {
                log.error("token production exception.", e);
                // 重启
                run();
            }
        }
    }

    /**
     * 关闭
     */
    public void shutdown() {
        interrupted = true;
    }
}
