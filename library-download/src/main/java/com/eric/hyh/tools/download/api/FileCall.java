package com.eric.hyh.tools.download.api;


import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.IDownloadProxy;
import com.eric.hyh.tools.download.internal.ServiceBridge;

/**
 * Created by Administrator on 2017/3/9.
 */

public class FileCall<T> {

    private FileRequest<T> request;
    private TaskInfo taskInfo;
    private IDownloadProxy.ILocalDownloadProxy mLocalAgent;
    private ServiceBridge serviceBridge;

    FileCall(FileRequest<T> request, IDownloadProxy.ILocalDownloadProxy localAgent, ServiceBridge serviceBridge, TaskInfo<T> taskInfo) {
        this.request = request;
        this.mLocalAgent = localAgent;
        this.serviceBridge = serviceBridge;
        this.taskInfo = taskInfo;
    }


    FileRequest<T> fileRequest() {
        return this.request;
    }

    public TaskInfo taskInfo() {
        return this.taskInfo;
    }

    void enqueue() {
        enqueue(null);
    }

    void enqueue(Callback callback) {
        if (request.byService()) {
            serviceBridge.request(request.command(), taskInfo, callback);
        } else {
            mLocalAgent.enqueue(request.command(), taskInfo, callback);
        }
    }
}

//{"persons":[{"name":"龙骑","age":10},{"name":"白虎","age":12},{"name":"火枪","age":14}],"location":"广州","age":100}