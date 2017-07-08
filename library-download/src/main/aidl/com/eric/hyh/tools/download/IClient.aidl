// IDownloadCallBack.aidl
package com.eric.hyh.tools.download;

// Declare any non-default types here with import statements
import com.eric.hyh.tools.download.bean.TaskInfo;
interface IClient {

    void onCall(in TaskInfo response);

    void otherProcessCommand(int command, String resKey);

    void onHaveNoTask();

    boolean isFileDownloading(String resKey);

    void onProcessChanged(boolean hasOtherProcess);
}
