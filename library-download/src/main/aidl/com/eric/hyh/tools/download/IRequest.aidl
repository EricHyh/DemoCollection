// IRequest.aidl
package com.eric.hyh.tools.download;

// Declare any non-default types here with import statements
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.ICallback;
interface IRequest {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void request(int command,in TaskInfo request);

    void requestOperateDB(in TaskInfo request);

    void registerAll(in ICallback callback);

    void correctDBErroStatus();

    void unRegisterAll();

}