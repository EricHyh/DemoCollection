package com.hyh.tools.download.internal;

import com.hyh.tools.download.net.HttpCall;
import com.hyh.tools.download.net.HttpCallback;
import com.hyh.tools.download.net.HttpResponse;
import com.hyh.tools.download.bean.TaskInfo;

import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */
abstract class AbstractHttpCallback implements HttpCallback {

    @Override
    public void onFailure(HttpCall httpCall, Exception e) {
    }

    @Override
    public void onResponse(HttpCall httpCall, HttpResponse httpResponse) throws IOException {
    }

    abstract TaskInfo getTaskInfo();

    abstract void pause();

    abstract void delete();
}
