package com.hyh.download.core;


import com.hyh.download.DownloadInfo;
import com.hyh.download.IFileChecker;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadProxy {

    void initProxy(Runnable afterInit);

    void startTask(RequestInfo taskInfo, IFileChecker fileChecker);

    void pauseTask(String resKey);

    void deleteTask(String resKey);

    boolean isTaskAlive(String resKey);

    boolean isFileDownloaded(String resKey, IFileChecker fileChecker);

    DownloadInfo getDownloadInfoByKey(String resKey);

}
