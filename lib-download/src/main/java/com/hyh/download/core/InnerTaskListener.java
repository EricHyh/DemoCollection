package com.hyh.download.core;

import com.hyh.download.db.bean.TaskInfo;

/**
 * Created by Eric_He on 2019/1/6.
 */

public interface InnerTaskListener {

    void onPrepare(TaskInfo taskInfo);

    void onWaitingInQueue(TaskInfo taskInfo);

    void onConnected(TaskInfo taskInfo);

    void onDownloading(TaskInfo taskInfo);

    void onRetrying(TaskInfo taskInfo);

    void onPause(TaskInfo taskInfo);

    void onDelete(TaskInfo taskInfo);

    void onSuccess(TaskInfo taskInfo);

    void onFailure(TaskInfo taskInfo);
    
}
