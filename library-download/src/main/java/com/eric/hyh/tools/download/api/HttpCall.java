package com.eric.hyh.tools.download.api;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface HttpCall {

    void enqueue(HttpCallback httpCallback);

    void cancel();

    boolean isExecuted();

    boolean isCanceled();

}
