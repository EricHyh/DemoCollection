package com.eric.hyh.tools.download.internal;


import com.eric.hyh.tools.download.ICallback;
import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadAgent {


    void enqueue(int command, TaskInfo taskInfo);

    void enqueue(int command, TaskInfo taskInfo, Callback callback);

    interface IServiceDownloadAgent extends IDownloadAgent {

        void setCallback(ICallback callback);

    }

    interface ILocalDownloadAgent extends IDownloadAgent {

        void setCallback(Callback callback);

    }
}
