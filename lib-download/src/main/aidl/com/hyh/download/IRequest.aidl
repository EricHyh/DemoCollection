// IRequest.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import  com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.IClient;
import com.hyh.download.IFileChecker;
//import java.util.Map;
interface IRequest {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean isAlive();

    void initDownloadProxy(int maxSynchronousDownloadNum);

    void onReceiveStartCommand(String resKey);

    void onReceivePauseCommand(String resKey);

    void onReceiveDeleteCommand(String resKey);

    void insertOrUpdate(in TaskInfo taskInfo);

    void register(int pid, in IClient client);

    boolean isTaskAlive(String resKey);

    boolean isFileDownloaded(String resKey);

    TaskInfo getTaskInfoByKey(String resKey);

    void startTask(in TaskInfo taskInfo, in IFileChecker fileChecker);

    void pauseTask(String resKey);

    void deleteTask(String resKey);

}