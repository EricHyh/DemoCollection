package com.eric.hyh.tools.download.internal;

import android.content.Context;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;


/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public abstract class LocalDownloadAgentImpl extends SuperDownloadAgent implements IDownloadAgent.ILocalDownloadAgent {

    private Callback mCallback;
    private ServiceBridge mServiceBridge;


    LocalDownloadAgentImpl(Context context, ServiceBridge serviceBridge) {
        super(context);
        this.mServiceBridge = serviceBridge;
    }

    @Override
    protected void handleHaveNoTask() {
        if (mCallback != null) {
            mCallback.onHaveNoTask();
        }
    }

    @Override
    protected void handleCallbackAndDB(TaskInfo taskInfo, Callback... callback) {
        if (callback != null && callback.length > 0) {
            for (Callback singleCallback : callback) {
                if (singleCallback != null) {
                    handleCallback(taskInfo, singleCallback);
                }
            }
        }
        if (mCallback != null) {
            handleCallback(taskInfo, mCallback);
        }
        handleDB(taskInfo);
    }

    @SuppressWarnings("unchecked")
    private void handleCallback(TaskInfo taskInfo, Callback callback) {

        switch (taskInfo.getCurrentStatus()) {
            case State.PREPARE:
                callback.onPrepare(taskInfo);
                break;
            case State.START_WRITE:
                callback.onFirstFileWrite(taskInfo);
                break;
            case State.DOWNLOADING:
                callback.onDownloading(taskInfo);
                break;
            case State.WAITING_IN_QUEUE:
                callback.onWaitingInQueue(taskInfo);
                break;
            case State.WAITING_FOR_WIFI:
                callback.onWaitingForWifi(taskInfo);
                break;
            case State.PAUSE:
                callback.onPause(taskInfo);
                break;
            case State.DELETE:
                callback.onDelete(taskInfo);
                break;
            case State.FAILURE:
                callback.onFailure(taskInfo);
                break;
            case State.SUCCESS:
                callback.onSuccess(taskInfo);
                break;
            case State.INSTALL:
                callback.onInstall(taskInfo);
                break;
            case State.UNINSTALL:
                callback.onUnInstall(taskInfo);
                break;
        }
    }

    private void handleDB(TaskInfo taskInfo) {
        mServiceBridge.requestOperateDB(taskInfo);
    }


    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }
}
