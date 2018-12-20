package com.hyh.download.core;


import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpCallback;
import com.hyh.download.net.HttpResponse;

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
