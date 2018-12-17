package com.hyh.download.core;

import com.hyh.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2018/12/17
 */

public interface DownloadCallback {

    void onFirstFileWrite(TaskInfo taskInfo);

    void onDownloading(TaskInfo taskInfo);



    void onFailure(TaskInfo taskInfo);

    void onSuccess(TaskInfo taskInfo);

}
