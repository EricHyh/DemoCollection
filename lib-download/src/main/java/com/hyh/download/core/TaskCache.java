package com.hyh.download.core;


import com.hyh.download.IFileChecker;
import com.hyh.download.db.bean.TaskInfo;

/**
 * Created by Administrator on 2017/3/16.
 */

public class TaskCache {

    int command;

    String resKey;

    TaskInfo taskInfo;

    IFileChecker fileChecker;

    TaskCache(int command, TaskInfo taskInfo, IFileChecker fileChecker) {
        this.command = command;
        this.taskInfo = taskInfo;
        this.fileChecker = fileChecker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCache taskCache = (TaskCache) o;
        return resKey.equals(taskCache.resKey);
    }
}
