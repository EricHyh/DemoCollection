package com.eric.hyh.tools.download.internal;


import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.FileDownloader;
import com.eric.hyh.tools.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public interface IDownloadProxy {


    void enqueue(int command, TaskInfo taskInfo);

    void setMaxSynchronousDownloadNum(int num);

    interface IServiceDownloadProxy extends IDownloadProxy {

    }

    interface ILocalDownloadProxy extends IDownloadProxy {

        void initProxy(FileDownloader.LockConfig lockConfig);

        void setAllTaskCallback(Callback callback);

        boolean isFileDownloading(String resKey);

        void operateDatebase(TaskInfo taskInfo);

        void destroy();
    }
}
