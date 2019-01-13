// IRequest.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import  com.hyh.download.core.DownloadProxyConfig;
import  com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.ITaskListener;
import com.hyh.download.IFileChecker;
//import java.util.Map;
interface IRequest {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean isAlive();

    void initDownloadProxy(in DownloadProxyConfig downloadProxyConfig, in IFileChecker globalFileChecker);

    void insertOrUpdate(in TaskInfo taskInfo);

    void register(int pid, in ITaskListener listener);

    boolean isTaskAlive(String resKey);

    boolean isFileDownloaded(String resKey, in IFileChecker fileChecker);

    TaskInfo getTaskInfoByKey(String resKey);

    void startTask(in TaskInfo taskInfo, in IFileChecker fileChecker);

    void pauseTask(String resKey);

    void deleteTask(String resKey);

}