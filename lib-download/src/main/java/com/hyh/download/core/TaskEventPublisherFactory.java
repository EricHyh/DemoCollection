package com.hyh.download.core;

import com.hyh.download.db.bean.TaskInfo;

/**
 * Created by Eric_He on 2018/12/31.
 */

public class TaskEventPublisherFactory {

    public static ITaskEventPublisher create(int threadMode) {
        return null;
    }


    public static class CurrentThreadPublisher implements ITaskEventPublisher {

        @Override
        public void publish(TaskInfo taskInfo) {
            //new Handler().post()
        }
    }
}
