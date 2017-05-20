package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.TaskInfo;

/**
 * Created by Administrator on 2017/3/16.
 */

public class TaskCache {

    int command;

    String resKey;

    TaskInfo taskInfo;

    Callback callback;

    TaskCache(int command, TaskInfo taskInfo, Callback callback) {
        this.command = command;
        this.taskInfo = taskInfo;
        this.callback = callback;
    }

    TaskCache(String resKey, TaskInfo taskInfo, Callback callback) {
        this.resKey = resKey;
        this.taskInfo = taskInfo;
        this.callback = callback;
    }

    TaskCache(String resKey) {
        this.resKey = resKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCache taskCache = (TaskCache) o;
        return resKey.equals(taskCache.resKey);
    }

}
