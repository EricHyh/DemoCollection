package com.hyh.download;


/**
 * Created by Eric_He on 2017/3/11.
 */

public abstract class CallbackAdapter implements Callback {

    @Override
    public void onPrepare(DownloadInfo downloadInfo) {

    }

    @Override
    public void onDownloading(DownloadInfo downloadInfo) {

    }

    @Override
    public void onWaitingInQueue(DownloadInfo downloadInfo) {

    }

    @Override
    public void onPause(DownloadInfo downloadInfo) {

    }

    @Override
    public void onDelete(DownloadInfo downloadInfo) {

    }

    @Override
    public void onSuccess(DownloadInfo downloadInfo) {

    }

    @Override
    public void onWaitingForWifi(DownloadInfo downloadInfo) {

    }

    @Override
    public void onLowDiskSpace(DownloadInfo downloadInfo) {

    }

    @Override
    public void onFailure(DownloadInfo downloadInfo) {

    }

    @Override
    public void onHaveNoTask() {

    }
}
