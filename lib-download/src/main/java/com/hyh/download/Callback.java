package com.hyh.download;

/**
 * Created by Administrator on 2017/3/9.
 */

public interface Callback {

    /**
     * 准备下载阶段
     *
     * @param downloadInfo 下载任务信息
     */
    void onPrepare(DownloadInfo downloadInfo);

    /**
     * 下载过程
     *
     * @param downloadInfo 下载任务信息
     */
    void onDownloading(DownloadInfo downloadInfo);

    /**
     * 等待过程
     *
     * @param downloadInfo 下载任务信息
     */
    void onWaitingInQueue(DownloadInfo downloadInfo);

    /**
     * 下载任务被暂停
     *
     * @param downloadInfo 下载任务信息
     */
    void onPause(DownloadInfo downloadInfo);


    /**
     * 下载任务被删除
     *
     * @param downloadInfo 下载任务信息
     */
    void onDelete(DownloadInfo downloadInfo);

    /**
     * 下载成功
     *
     * @param downloadInfo 下载任务信息
     */
    void onSuccess(DownloadInfo downloadInfo);

    /**
     * 等待wifi过程
     *
     * @param downloadInfo 下载任务信息
     */
    void onWaitingForWifi(DownloadInfo downloadInfo);

    /**
     * 磁盘空间不足
     *
     * @param downloadInfo 下载任务信息
     */
    void onLowDiskSpace(DownloadInfo downloadInfo);

    /**
     * 下载失败
     *
     * @param downloadInfo 下载任务信息
     */
    void onFailure(DownloadInfo downloadInfo);

}
