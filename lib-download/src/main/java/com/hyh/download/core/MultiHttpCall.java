package com.hyh.download.core;


import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpCallback;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2017/7/12
 */
class MultiHttpCall implements HttpCall {

    private Map<String, HttpCall> httpCallMap;

    private List<RangeInfo> rangeInfoList;

    MultiHttpCall(Map<String, HttpCall> httpCallMap, List<RangeInfo> rangeInfoList) {
        this.httpCallMap = httpCallMap;
        this.rangeInfoList = rangeInfoList;
    }

    boolean isHttpCallEmpty() {
        return httpCallMap == null || httpCallMap.isEmpty();
    }

    @Override
    public void enqueue(HttpCallback httpCallback) {
        MultiHttpCallbackWrapper multiHttpCallbackWrapper = (MultiHttpCallbackWrapper) httpCallback;
        multiHttpCallbackWrapper.setRangeInfoList(rangeInfoList);
        Set<Map.Entry<String, HttpCall>> entrySet = httpCallMap.entrySet();
        for (Map.Entry<String, HttpCall> httpCallEntry : entrySet) {
            String tag = httpCallEntry.getKey();
            HttpCall httpCall = httpCallEntry.getValue();
            httpCall.enqueue(multiHttpCallbackWrapper.getHttpCallback(tag));
        }
    }

    @Override
    public void cancel() {
        Collection<HttpCall> httpCalls = httpCallMap.values();
        for (HttpCall httpCall : httpCalls) {
            if (!httpCall.isCanceled()) {
                httpCall.cancel();
            }
        }
    }

    @Override
    public boolean isExecuted() {
        Collection<HttpCall> httpCalls = httpCallMap.values();
        for (HttpCall httpCall : httpCalls) {
            if (httpCall.isExecuted()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCanceled() {
        Collection<HttpCall> httpCalls = httpCallMap.values();
        for (HttpCall httpCall : httpCalls) {
            if (!httpCall.isCanceled()) {
                return false;
            }
        }
        return true;
    }
}
