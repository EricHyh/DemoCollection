package com.hyh.download;

import com.hyh.download.bean.TaskInfo;

/**
 * Created by Administrator on 2017/3/9.
 */

public interface Callback {

    /**
     * 准备下载阶段
     *
     * @param taskInfo 下载任务信息
     */
    void onPrepare(TaskInfo taskInfo);

    /**
     * 磁盘空间不足
     *
     * @param taskInfo 下载任务信息
     */
    void onNoEnoughSpace(TaskInfo taskInfo);

    /**
     * 从网络中获取到输入流并开始往文件中写数据
     *
     * @param taskInfo 下载任务信息
     */
    void onFirstFileWrite(TaskInfo taskInfo);

    /**
     * 下载过程
     *
     * @param taskInfo 下载任务信息
     */
    void onDownloading(TaskInfo taskInfo);


    /**
     * 等待过程
     *
     * @param taskInfo 下载任务信息
     */
    void onWaitingInQueue(TaskInfo taskInfo);


    /**
     * 等待wifi过程
     *
     * @param taskInfo 下载任务信息
     */
    void onWaitingForWifi(TaskInfo taskInfo);


    /**
     * 下载任务被删除
     *
     * @param taskInfo 下载任务信息
     */
    void onDelete(TaskInfo taskInfo);

    /**
     * 下载任务被暂停
     *
     * @param taskInfo 下载任务信息
     */
    void onPause(TaskInfo taskInfo);

    /**
     * 下载成功
     *
     * @param taskInfo 下载任务信息
     */
    void onSuccess(TaskInfo taskInfo);

    /**
     * 下载失败
     *
     * @param taskInfo 下载任务信息
     */
    void onFailure(TaskInfo taskInfo);

    /**
     * 任务结束
     */
    void onHaveNoTask();

}
