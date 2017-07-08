// IRequest.aidl
package com.eric.hyh.tools.download;

// Declare any non-default types here with import statements
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.IClient;
interface IRequest {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void request(int pid, int command, in TaskInfo request);

    void onCall(int pid, in TaskInfo request);

    void register(int pid, in IClient client);

    void correctDBErroStatus();

    void unRegister(int pid);

    boolean isFileDownloading(int pid, String resKey);

}