package com.github.cwdtom.gateway.environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.cwdtom.gateway.filter.AfterFilter;
import com.github.cwdtom.gateway.filter.BeforeFilter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.LinkedList;
import java.util.List;

/**
 * 过滤器环境
 *
 * @author chenweidong
 * @since 2.2.0
 */
public class FilterEnvironment {
    /**
     * 前置过滤列表
     */
    private List<BeforeFilter> beforeFilters = new LinkedList<>();
    /**
     * 后置过滤列表
     */
    private List<AfterFilter> afterFilters = new LinkedList<>();

    FilterEnvironment(ConfigEnvironment config) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException {
        JSONObject obj = JSON.parseObject(config.getChild("filter"));
        if (obj != null) {
            JSONArray arr = obj.getJSONArray("before");
            if (arr != null) {
                for (int i = 0; i < arr.size(); ++i) {
                    String className = arr.getString(i);
                    Class clazz = Class.forName(className);
                    beforeFilters.add((BeforeFilter) clazz.newInstance());
                }
            }
            arr = obj.getJSONArray("after");
            if (arr != null) {
                for (int i = 0; i < arr.size(); ++i) {
                    String className = arr.getString(i);
                    Class clazz = Class.forName(className);
                    afterFilters.add((AfterFilter) clazz.newInstance());
                }
            }
        }
    }

    /**
     * 前置过滤
     *
     * @param request 请求体
     * @param content 请求内容
     * @return 是否拦截
     */
    public boolean beforeFilter(FullHttpRequest request, byte[] content) {
        for (BeforeFilter filter : beforeFilters) {
            boolean isContinue = filter.filter(request, content);
            if (!isContinue) {
                return false;
            }
        }
        return true;
    }

    /**
     * 后置过滤器
     *
     * @param response 响应
     */
    public void afterFilter(FullHttpResponse response) {
        for (AfterFilter filter : afterFilters) {
            filter.filter(response);
        }
    }
}
