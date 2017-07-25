package com.hyh.tools.download.api;


import com.hyh.tools.download.bean.TaskInfo;

/**
 * Created by Eric_He on 2017/3/11.
 */

public abstract class CallbackAdapter<T> implements Callback<T> {

    @Override
    public void onNoEnoughSpace(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onPrepare(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onFirstFileWrite(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onDownloading(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onWaitingInQueue(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onWaitingForWifi(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onDelete(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onPause(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onSuccess(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onInstall(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onUnInstall(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onFailure(TaskInfo<T> taskInfo) {

    }

    @Override
    public void onHaveNoTask() {

    }
}
