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
 * filter environment
 *
 * @author chenweidong
 * @since 2.2.0
 */
public class FilterEnvironment {
    /**
     * pre filter list
     */
    private List<BeforeFilter> beforeFilters = new LinkedList<>();
    /**
     * post filter list
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
     * pre filters
     *
     * @param request request
     * @param content request context
     * @return continue or not
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
     * post filters
     *
     * @param response response
     */
    public void afterFilter(FullHttpResponse response) {
        for (AfterFilter filter : afterFilters) {
            filter.filter(response);
        }
    }
}
