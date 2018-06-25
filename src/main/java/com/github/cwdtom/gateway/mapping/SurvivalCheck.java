package com.github.cwdtom.gateway.mapping;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.util.HttpUtils;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
    private static List<OfflineMapper> mappers = new Vector<>();
    /**
     * 是否中断
     */
    private boolean interrupted = false;

    /**
     * 添加已熔断mapper
     *
     * @param mapper mapper
     */
    public static void add(OfflineMapper mapper) {
        if (!mappers.contains(mapper)) {
            mappers.add(mapper);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Iterator<OfflineMapper> iterator = mappers.iterator();
                while (iterator.hasNext()) {
                    OfflineMapper o = iterator.next();
                    try {
                        String url = Constant.HTTP_PREFIX + o.getMapper().getTarget() + o.getUri();
                        if (o.getMethod().equals(HttpMethod.GET)) {
                            HttpUtils.sendGet(url);
                        } else if (o.getMethod().equals(HttpMethod.POST)) {
                            HttpUtils.sendPost(url, new byte[0], o.getContentType());
                        }
                        // 服务恢复
                        o.getMapper().setExceptionCount(0);
                        iterator.remove();
                    } catch (IOException ignored) {
                    }
                }
                if (interrupted) {
                    return;
                }
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            log.error("存活检查服务异常！", e);
            run();
        }
    }

    /**
     * 关闭服务
     */
    public void shutdown() {
        interrupted = true;
    }
}
