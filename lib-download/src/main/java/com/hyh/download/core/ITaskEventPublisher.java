package com.hyh.download.core;

import com.hyh.download.db.bean.TaskInfo;

/**
 * Created by Eric_He on 2018/12/31.
 */

public interface ITaskEventPublisher {

    void publish(TaskInfo taskInfo);

}
