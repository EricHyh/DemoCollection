package com.hyh.download.core;

/**
 * @author Administrator
 * @description
 * @data 2018/12/29
 */

public interface IRetryStrategy {

    boolean shouldRetry(onWaitingListener listener);

    void clearCurrentRetryTimes();

    void cancel();

    interface onWaitingListener {

        void onWaiting();

    }
}