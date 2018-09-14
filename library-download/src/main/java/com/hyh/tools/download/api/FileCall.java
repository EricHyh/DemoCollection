package com.hyh.tools.download.api;


import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.internal.IDownloadProxy;

/**
 * Created by Administrator on 2017/3/9.
 */

public class FileCall {

    private FileRequest request;
    private TaskInfo taskInfo;
    private IDownloadProxy.ILocalDownloadProxy mDownloadProxy;

    FileCall(FileRequest request, IDownloadProxy.ILocalDownloadProxy downloadProxy, TaskInfo taskInfo) {
        this.request = request;
        this.mDownloadProxy = downloadProxy;
        this.taskInfo = taskInfo;
    }


    FileRequest fileRequest() {
        return this.request;
    }

    public TaskInfo taskInfo() {
        return this.taskInfo;
    }

    void enqueue() {
        mDownloadProxy.enqueue(request.command(), taskInfo);
    }
}