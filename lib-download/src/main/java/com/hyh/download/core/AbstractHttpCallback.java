package com.hyh.download.core;


import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpCallback;
import com.hyh.download.net.HttpResponse;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */
abstract class AbstractHttpCallback implements HttpCallback {

    @Override
    public void onResponse(HttpCall httpCall, HttpResponse httpResponse) {
    }

    @Override
    public void onFailure(HttpCall httpCall, Exception e) {
    }

    abstract void cancel();

}
