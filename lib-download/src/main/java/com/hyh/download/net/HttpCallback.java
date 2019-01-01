package com.hyh.download.net;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpCallback {

    void onResponse(HttpCall httpCall, HttpResponse httpResponse);

    void onFailure(HttpCall httpCall, Exception e);

}
