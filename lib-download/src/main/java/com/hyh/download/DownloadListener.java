package com.hyh.download;

import java.util.List;
import java.util.Map;

/**
 * Created by Eric_He on 2019/1/1.
 */

public interface DownloadListener {

    void onPrepare(DownloadInfo downloadInfo);

    void onWaitingInQueue(DownloadInfo downloadInfo);

    void onConnected(DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields);

    void onDownloading(String resKey, long currentSize, long totalSize, int progress, float speed);

    void onPause(DownloadInfo downloadInfo);

    void onDelete(DownloadInfo downloadInfo);

    void onSuccess(DownloadInfo downloadInfo);

    void onFailure(DownloadInfo downloadInfo, int failureCode);

}
