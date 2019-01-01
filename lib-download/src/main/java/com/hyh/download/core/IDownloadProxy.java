package com.hyh.download.core;


import com.hyh.download.IFileChecker;
import com.hyh.download.db.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadProxy {

    void initProxy(Runnable afterInit);

    void startTask(TaskInfo taskInfo, IFileChecker fileChecker);

    void pauseTask(String resKey);

    void deleteTask(String resKey);

    boolean isTaskAlive(String resKey);

    boolean isFileDownloaded(String resKey, IFileChecker fileChecker);

    boolean isFileDownloaded(String resKey, int versionCode, IFileChecker fileChecker);

    void insertOrUpdate(TaskInfo taskInfo);

    TaskInfo getTaskInfoByKey(String resKey);

}
