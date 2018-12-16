package com.hyh.download.core;

import android.content.Context;

import com.hyh.download.Callback;
import com.hyh.download.State;
import com.hyh.download.bean.TaskInfo;


/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class LocalDownloadProxyImpl extends SuperDownloadProxy implements IDownloadProxy {

    private Callback mCallback;

    public LocalDownloadProxyImpl(Context context, int maxSynchronousDownloadNum, Callback callback) {
        super(context, maxSynchronousDownloadNum);
        mCallback = callback;
    }

    @Override
    protected void handleCallback(TaskInfo taskInfo) {
        switch (taskInfo.getCurrentStatus()) {
            case State.PREPARE:
                mCallback.onPrepare(taskInfo);
                break;
            case State.START_WRITE:
                mCallback.onFirstFileWrite(taskInfo);
                break;
            case State.DOWNLOADING:
                mCallback.onDownloading(taskInfo);
                break;
            case State.WAITING_IN_QUEUE:
                mCallback.onWaitingInQueue(taskInfo);
                break;
            case State.WAITING_FOR_WIFI:
                mCallback.onWaitingForWifi(taskInfo);
                break;
            case State.PAUSE:
                mCallback.onPause(taskInfo);
                break;
            case State.DELETE:
                mCallback.onDelete(taskInfo);
                break;
            case State.FAILURE:
                mCallback.onFailure(taskInfo);
                break;
            case State.SUCCESS:
                mCallback.onSuccess(taskInfo);
                break;
        }
    }

    @Override
    protected void handleHaveNoTask() {
        if (mCallback != null) {
            mCallback.onHaveNoTask();
        }
    }
}
