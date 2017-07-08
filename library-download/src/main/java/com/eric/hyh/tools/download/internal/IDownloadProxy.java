package com.eric.hyh.tools.download.internal;


import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadProxy {


    void enqueue(int command, TaskInfo taskInfo);

    void enqueue(int command, TaskInfo taskInfo, Callback callback);

    interface IServiceDownloadProxy extends IDownloadProxy {

    }

    interface ILocalDownloadProxy extends IDownloadProxy {

        void setCallback(Callback callback);

    }
}
