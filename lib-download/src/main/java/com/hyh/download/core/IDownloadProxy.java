package com.hyh.download.core;


import com.hyh.download.FileChecker;
import com.hyh.download.db.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadProxy {

    void initProxy(Runnable afterInit);

    void onReceiveStartCommand(String resKey);

    void onReceivePauseCommand(String resKey);

    void onReceiveDeleteCommand(String resKey);

    void startTask(TaskInfo taskInfo, FileChecker fileChecker);

    void pauseTask(String resKey);

    void deleteTask(String resKey);

    boolean isTaskAlive(String resKey);

    boolean isFileDownloaded(String resKey);

    void insertOrUpdate(TaskInfo taskInfo);

    TaskInfo getTaskInfoByKey(String resKey);

}
