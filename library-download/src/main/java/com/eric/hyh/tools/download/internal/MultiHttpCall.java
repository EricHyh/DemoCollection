package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.HttpCall;
import com.eric.hyh.tools.download.api.HttpCallback;

import java.util.Collection;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/7/12
 */

class MultiHttpCall implements HttpCall {

    private Map<String, HttpCall> httpCallMap;

    MultiHttpCall(Map<String, HttpCall> httpCallMap) {
        this.httpCallMap = httpCallMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void enqueue(HttpCallback httpCallback) {
        /*Set<Map.Entry<String, HttpCall>> entrySet = httpCallMap.entrySet();
        for (Map.Entry<String, HttpCall> httpCallEntry : entrySet) {
            String tag = httpCallEntry.getKey();
            HttpCall httpCall = httpCallEntry.getValue();
            httpCall.enqueue(multiHttpCallbackImpl.getHttpCallback(tag));
        }*/
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
