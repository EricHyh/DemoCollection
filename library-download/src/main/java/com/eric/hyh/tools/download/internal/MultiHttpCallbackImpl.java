package com.eric.hyh.tools.download.internal;

import android.content.Context;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.HttpCallback;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.util.Collection;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/7/12
 */

class MultiHttpCallbackImpl extends HttpCallbackImpl {

    private Map<String, HttpCallbackImpl> httpCallbackMap;

    MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, Callback downloadCallback) {
        super(context, client, taskInfo, downloadCallback);
    }

    HttpCallback getHttpCallback(String tag) {
        return httpCallbackMap.get(tag);
    }

    @Override
    protected void setPause(boolean pause) {
        Collection<HttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (HttpCallbackImpl httpCallback : httpCallbacks) {
            httpCallback.setPause(pause);
        }
    }

    @Override
    protected void setDelete(boolean delete) {
        Collection<HttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (HttpCallbackImpl httpCallback : httpCallbacks) {
            httpCallback.setDelete(delete);
        }
    }
}
