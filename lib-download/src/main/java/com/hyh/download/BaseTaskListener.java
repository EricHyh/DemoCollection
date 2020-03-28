package com.hyh.download;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2019/8/13
 */

public class BaseTaskListener implements TaskListener {
    @Override
    public void onPrepare(DownloadInfo downloadInfo) {

    }

    @Override
    public void onWaitingStart(DownloadInfo downloadInfo) {

    }

    @Override
    public void onWaitingEnd(DownloadInfo downloadInfo) {

    }

    @Override
    public void onConnected(DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields) {

    }

    @Override
    public void onDownloading(String resKey, long totalSize, long currentSize, int progress, float speed) {

    }

    @Override
    public void onRetrying(DownloadInfo downloadInfo, boolean deleteFile) {

    }

    @Override
    public void onPause(DownloadInfo downloadInfo) {
        if (isAutoRemoveWhenTaskEnd()) {
            FileDownloader.getInstance().removeDownloadListener(downloadInfo.getResKey(), this);
        }
    }

    @Override
    public void onDelete(DownloadInfo downloadInfo) {
        if (isAutoRemoveWhenTaskEnd()) {
            FileDownloader.getInstance().removeDownloadListener(downloadInfo.getResKey(), this);
        }
    }

    @Override
    public void onSuccess(DownloadInfo downloadInfo) {
        if (isAutoRemoveWhenTaskEnd()) {
            FileDownloader.getInstance().removeDownloadListener(downloadInfo.getResKey(), this);
        }
    }

    @Override
    public void onFailure(DownloadInfo downloadInfo) {
        if (isAutoRemoveWhenTaskEnd()) {
            FileDownloader.getInstance().removeDownloadListener(downloadInfo.getResKey(), this);
        }
    }

    protected boolean isAutoRemoveWhenTaskEnd() {
        return false;
    }
}
