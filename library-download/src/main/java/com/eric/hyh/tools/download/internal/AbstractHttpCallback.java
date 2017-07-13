package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.HttpCallback;
import com.eric.hyh.tools.download.bean.TaskInfo;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */
abstract class AbstractHttpCallback implements HttpCallback {

    TaskInfo taskInfo;

    AbstractHttpCallback(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    abstract void pause();

    abstract void delete();
}
