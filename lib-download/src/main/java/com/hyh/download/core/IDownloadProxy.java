package com.hyh.download.core;


import com.hyh.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadProxy {

    void initProxy();

    void enqueue(int command, TaskInfo taskInfo);

    void setMaxSynchronousDownloadNum(int num);

    boolean isTaskEnqueue(String resKey);

    void onReceiveStartCommand(TaskInfo taskInfo);

    void onReceivePauseCommand(TaskInfo taskInfo);

    void onReceiveDeleteCommand(TaskInfo taskInfo);

    boolean isFileDownloading(String resKey);

    boolean isFileDownloaded(String resKey);

    TaskInfo getTaskInfoByKey(String resKey);

    void deleteTask(String resKey);

    void operateDatabase(TaskInfo taskInfo);

}
