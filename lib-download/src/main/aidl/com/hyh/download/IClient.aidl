// IDownloadCallBack.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import  com.hyh.download.bean.TaskInfo;
interface IClient {

    boolean isAlive();

    void onCallback(in TaskInfo taskInfo);

    void onHaveNoTask();

}
