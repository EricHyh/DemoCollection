// IDownloadCallBack.aidl
package com.eric.hyh.tools.download;

// Declare any non-default types here with import statements
import com.eric.hyh.tools.download.bean.TaskInfo;
interface ICallback {

    void onCall(in TaskInfo response);

    void onHaveNoTask();
}
