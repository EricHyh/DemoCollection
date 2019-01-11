package com.hyh.filedownloader.sample.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 * @description
 * @data 2019/1/3
 */

public class HttpCallBuilder {

    private ThreadPoolExecutor executor;

    private String url;

    private String method;

    private Map<String, String> urlParams = new HashMap<>();

    private Map<String, List<String>> requestHeaders = new HashMap<>();

    private RequestBody requestBody;

    private int connectTimeout;

    private int readTimeout;

    private Object tag;

    HttpCallBuilder(ThreadPoolExecutor executor, String url) {
        this.executor = executor;
        this.url = url;
    }

    public HttpCallBuilder method(String method) {
        this.method = method;
        return this;
    }

    public HttpCallBuilder urlParam(String key, String value) {
        urlParams.put(key, value);
        return this;
    }

    public HttpCallBuilder urlParams(Map<String, String> urlParams) {
        this.urlParams.putAll(urlParams);
        return this;
    }

    public HttpCallBuilder requestHeader(String key, String value) {
        List<String> list = requestHeaders.get(key);
        if (list != null) {
            list.add(value);
        } else {
            list = new ArrayList<>();
            list.add(value);
            requestHeaders.put(key, list);
        }
        return this;
    }

    public HttpCallBuilder requestHeaders(Map<String, List<String>> requestHeaders) {
        Set<Map.Entry<String, List<String>>> entrySet = requestHeaders.entrySet();
        for (Map.Entry<String, List<String>> entry : entrySet) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            List<String> list = requestHeaders.get(key);
            if (list != null) {
                list.addAll(value);
            } else {
                list = new ArrayList<>(value);
                requestHeaders.put(key, list);
            }
        }
        return this;
    }

    public HttpCallBuilder requestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public HttpCallBuilder connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public HttpCallBuilder readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public HttpCallBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public HttpCall call() {
        HttpCall httpCall = new HttpCall();
        httpCall.executor = this.executor;
        httpCall.url = this.url;
        httpCall.method = this.method;
        httpCall.urlParams = this.urlParams;
        httpCall.requestHeaders = this.requestHeaders;
        httpCall.requestBody = this.requestBody;
        httpCall.connectTimeout = this.connectTimeout;
        httpCall.readTimeout = this.readTimeout;
        httpCall.tag = this.tag;
        return httpCall;
    }
}
