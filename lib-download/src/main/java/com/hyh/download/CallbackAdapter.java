package com.hyh.download;


import com.hyh.download.bean.TaskInfo;

/**
 * Created by Eric_He on 2017/3/11.
 */

public abstract class CallbackAdapter implements Callback {

    @Override
    public void onPrepare(TaskInfo taskInfo) {

    }

    @Override
    public void onNoEnoughSpace(TaskInfo taskInfo) {

    }

    @Override
    public void onFirstFileWrite(TaskInfo taskInfo) {

    }

    @Override
    public void onDownloading(TaskInfo taskInfo) {

    }

    @Override
    public void onWaitingInQueue(TaskInfo taskInfo) {

    }

    @Override
    public void onWaitingForWifi(TaskInfo taskInfo) {

    }

    @Override
    public void onDelete(TaskInfo taskInfo) {

    }

    @Override
    public void onPause(TaskInfo taskInfo) {

    }

    @Override
    public void onSuccess(TaskInfo taskInfo) {

    }

    @Override
    public void onFailure(TaskInfo taskInfo) {

    }

    @Override
    public void onHaveNoTask() {

    }
}
