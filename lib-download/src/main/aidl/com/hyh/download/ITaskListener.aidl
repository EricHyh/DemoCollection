// ITaskListener.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import com.hyh.download.DownloadInfo;

interface ITaskListener {

    boolean isAlive();

    void onPrepare(in DownloadInfo downloadInfo);

    void onWaitingInQueue(in DownloadInfo downloadInfo);

    void onConnected(in DownloadInfo downloadInfo, in Map responseHeaderFields);

    void onDownloading(String resKey, long totalSize, long currentSize, int progress, float speed);

    void onRetrying(in DownloadInfo downloadInfo, boolean deleteFile);

    void onPause(in DownloadInfo downloadInfo);

    void onDelete(in DownloadInfo downloadInfo);

    void onSuccess(in DownloadInfo downloadInfo);

    void onFailure(in DownloadInfo downloadInfo);

}
