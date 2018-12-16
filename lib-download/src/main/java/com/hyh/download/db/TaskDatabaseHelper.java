package com.hyh.download.db;

import android.content.Context;

import com.hyh.download.bean.TaskInfo;

import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class TaskDatabaseHelper {

    public static TaskDatabaseHelper getInstance() {
        return new TaskDatabaseHelper();
    }

    public TaskDatabaseHelper init(Context context) {
        return null;
    }

    public void fixDatabaseErrorStatus() {
    }

    public void insertOrUpdate(TaskInfo taskInfo) {
    }

    public void delete(String resKey) {

    }

    public void delete(TaskInfo taskInfo) {

    }

    public Map<String, TaskInfo> getAllTask() {
        return null;
    }

    public TaskInfo getTaskInfoByKey(String resKey) {
        return null;
    }
}

