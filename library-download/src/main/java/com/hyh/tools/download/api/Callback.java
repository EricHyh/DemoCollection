package com.hyh.tools.download.api;


import com.hyh.tools.download.bean.TaskInfo;

/**
 * Created by Administrator on 2017/3/9.
 */

public interface Callback<T> {


    /**
     * 磁盘空间不足
     *
     * @param taskInfo 下载任务信息
     */
    void onNoEnoughSpace(TaskInfo<T> taskInfo);

    /**
     * 准备下载阶段
     *
     * @param taskInfo 下载任务信息
     */
    void onPrepare(TaskInfo<T> taskInfo);

    /**
     * 从网络中获取到输入流并开始往文件中写数据
     *
     * @param taskInfo 下载任务信息
     */
    void onFirstFileWrite(TaskInfo<T> taskInfo);

    /**
     * 下载过程
     *
     * @param taskInfo 下载任务信息
     */
    void onDownloading(TaskInfo<T> taskInfo);


    /**
     * 等待过程
     *
     * @param taskInfo 下载任务信息
     */
    void onWaitingInQueue(TaskInfo<T> taskInfo);


    /**
     * 等待wifi过程
     *
     * @param taskInfo 下载任务信息
     */
    void onWaitingForWifi(TaskInfo<T> taskInfo);


    /**
     * 下载任务被删除
     *
     * @param taskInfo 下载任务信息
     */
    void onDelete(TaskInfo<T> taskInfo);

    /**
     * 下载任务被暂停
     *
     * @param taskInfo 下载任务信息
     */
    void onPause(TaskInfo<T> taskInfo);

    /**
     * 下载成功
     *
     * @param taskInfo 下载任务信息
     */
    void onSuccess(TaskInfo<T> taskInfo);

    /**
     * 安装成功
     *
     * @param taskInfo 下载任务信息
     */
    void onInstall(TaskInfo<T> taskInfo);


    /**
     * 卸载成功
     *
     * @param taskInfo 下载任务信息
     */
    void onUnInstall(TaskInfo<T> taskInfo);

    /**
     * 下载失败
     *
     * @param taskInfo 下载任务信息
     */
    void onFailure(TaskInfo<T> taskInfo);

    /**
     * 任务结束
     */
    void onHaveNoTask();


}
