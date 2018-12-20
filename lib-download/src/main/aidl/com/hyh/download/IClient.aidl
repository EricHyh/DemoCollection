// IDownloadCallBack.aidl
package com.hyh.download;

// Declare any non-default types here with import statements
import com.hyh.download.DownloadInfo;
interface IClient {

    boolean isAlive();

    void onCallback(in DownloadInfo downloadInfo);

    void onHaveNoTask();

}
