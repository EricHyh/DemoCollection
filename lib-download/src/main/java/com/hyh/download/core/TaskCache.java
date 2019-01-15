package com.hyh.download.core;


import com.hyh.download.IFileChecker;

/**
 * Created by Administrator on 2017/3/16.
 */

public class TaskCache {

    int command;

    String resKey;

    RequestInfo requestInfo;

    IFileChecker fileChecker;

    TaskCache(int command, RequestInfo requestInfo, IFileChecker fileChecker) {
        this.command = command;
        this.requestInfo = requestInfo;
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
