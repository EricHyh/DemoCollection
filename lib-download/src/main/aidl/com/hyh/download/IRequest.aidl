// IRequest.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import  com.hyh.download.bean.TaskInfo;
import com.hyh.download.IClient;
//import java.util.Map;
interface IRequest {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void fixDatabaseErrorStatus();

    void onReceiveStartCommand(in TaskInfo taskInfo);

    void onReceivePauseCommand(in TaskInfo taskInfo);

    void onReceiveDeleteCommand(in TaskInfo taskInfo);

    void request(int pid, int command, in TaskInfo taskInfo);

    void operateDatabase(int pid, in TaskInfo taskInfo);

    void register(int pid, in IClient client);

    boolean isTaskEnqueue(String resKey);

}