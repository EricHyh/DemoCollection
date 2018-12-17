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
                mCallback.onPrepare(taskInfo.toDownloadInfo());
                break;
            case State.START_WRITE:
                mCallback.onFirstFileWrite(taskInfo.toDownloadInfo());
                break;
            case State.DOWNLOADING:
                mCallback.onDownloading(taskInfo.toDownloadInfo());
                break;
            case State.WAITING_IN_QUEUE:
                mCallback.onWaitingInQueue(taskInfo.toDownloadInfo());
                break;
            case State.WAITING_FOR_WIFI:
                mCallback.onWaitingForWifi(taskInfo.toDownloadInfo());
                break;
            case State.PAUSE:
                mCallback.onPause(taskInfo.toDownloadInfo());
                break;
            case State.DELETE:
                mCallback.onDelete(taskInfo.toDownloadInfo());
                break;
            case State.FAILURE:
                mCallback.onFailure(taskInfo.toDownloadInfo());
                break;
            case State.SUCCESS:
                mCallback.onSuccess(taskInfo.toDownloadInfo());
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
