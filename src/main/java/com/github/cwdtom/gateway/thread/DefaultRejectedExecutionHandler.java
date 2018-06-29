package com.github.cwdtom.gateway.thread;

import com.github.cwdtom.gateway.handler.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 默认线程池拒绝操作
 *
 * @author chenweidong
 * @since 1.7.2
 */
@Slf4j
public class DefaultRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (r instanceof RequestHandler) {
            ((RequestHandler) r).release(true);
        }
        log.warn("MISSION REJECTED.", executor);
    }
}
