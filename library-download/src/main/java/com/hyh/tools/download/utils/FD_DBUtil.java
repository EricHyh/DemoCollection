package com.hyh.tools.download.utils;

import android.content.Context;
import android.text.TextUtils;

import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.db.Database;
import com.hyh.tools.download.db.DatabaseFactory;
import com.hyh.tools.download.db.bean.TaskDBInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @description
 * @data 2018/2/7
 */

public class FD_DBUtil {

    private static volatile FD_DBUtil sFD_DBUtil;

    private Database mDatabase;

    private Executor mDatabaseExecutor;

    private final ConcurrentHashMap<String, TaskDBInfo> mTaskDBInfoContainer = new ConcurrentHashMap<>();

    public static FD_DBUtil getInstance(Context context) {
        if (sFD_DBUtil != null) {
            return sFD_DBUtil;
        }
        synchronized (FD_DBUtil.class) {
            if (sFD_DBUtil == null) {
                sFD_DBUtil = new FD_DBUtil(context);
            }
            return sFD_DBUtil;
        }
    }

    private FD_DBUtil(Context context) {
        this.mDatabase = DatabaseFactory.create(context);
        this.mDatabaseExecutor = FD_Utils.buildExecutor(1, 1, 120, "Database Thread", true);
    }


    public void operate(final TaskInfo taskInfo) {
        if (taskInfo.getCurrentStatus() == State.DELETE || taskInfo.getCurrentStatus() == State.UNINSTALL) {
            delete(taskInfo);
        } else {
            insertOrReplace(taskInfo);
        }
    }


    private void insertOrReplace(final TaskInfo taskInfo) {
        mDatabaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TaskDBInfo newTaskDBInfo = taskInfo2TaskDBInfo(taskInfo);
                mDatabase.insertOrReplace(newTaskDBInfo);
            }
        });
    }

    public synchronized void insertOrReplace(final TaskDBInfo taskDBInfo) {
        mDatabaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.insertOrReplace(taskDBInfo);
            }
        });
    }


    private void delete(final TaskInfo taskInfo) {
        mDatabaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TaskDBInfo newTaskDBInfo = taskInfo2TaskDBInfo(taskInfo);
                mDatabase.delete(newTaskDBInfo);
            }
        });
    }

    public synchronized void delete(final TaskDBInfo taskDBInfo) {
        mDatabaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.delete(taskDBInfo);
            }
        });
    }


    public synchronized Map<String, TaskDBInfo> getAllTaskMap() {
        Map<String, TaskDBInfo> historyTasks = new ConcurrentHashMap<>();
        List<TaskDBInfo> taskDBInfos = mDatabase.selectAll();
        if (taskDBInfos != null && taskDBInfos.size() > 0) {
            for (TaskDBInfo taskDBInfo : taskDBInfos) {
                historyTasks.put(taskDBInfo.getResKey(), taskDBInfo);
            }
        }
        return historyTasks;
    }


    public synchronized List<TaskDBInfo> getWaitingForWifiTasks() {
        return getTasks(State.WAITING_FOR_WIFI);
    }


    public synchronized List<TaskDBInfo> getSuccessTasks() {
        return getTasks(State.SUCCESS);
    }

    public synchronized List<TaskDBInfo> getInstalledTasks() {
        return getTasks(State.INSTALL);
    }

    public synchronized List<TaskDBInfo> getAllTaskList() {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectAll();
        return taskDBInfos == null ? new ArrayList<TaskDBInfo>() : taskDBInfos;
    }

    public synchronized boolean reviseDateBaseErroStatus(Context context) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectAll();
        if (taskDBInfos.isEmpty()) {
            return true;
        }
        for (TaskDBInfo taskDBInfo : taskDBInfos) {
            File file = new File(taskDBInfo.getFilePath());
            if (!file.exists()) {//可能是文件被删除了
                if (TextUtils.isEmpty(taskDBInfo.getPackageName())
                        || !FD_PackageUtil.isAppInstall(context, taskDBInfo.getPackageName())) {//不是因为应用安装后删除文件的情况
                    Long currentSize = taskDBInfo.getCurrentSize();
                    if (currentSize != null && currentSize > 0) {
                        taskDBInfo.setCurrentSize(0L);
                        taskDBInfo.setProgress(0);
                        taskDBInfo.setCurrentStatus(State.PAUSE);
                        mDatabase.insertOrReplace(taskDBInfo);
                        continue;
                    }
                }
            }
            if (taskDBInfo.getCurrentStatus() == State.PREPARE || taskDBInfo.getCurrentStatus() == State.WAITING_IN_QUEUE
                    || taskDBInfo.getCurrentStatus() == State.START_WRITE || taskDBInfo.getCurrentStatus() == State.DOWNLOADING) {
                taskDBInfo.setCurrentStatus(State.PAUSE);
                mDatabase.insertOrReplace(taskDBInfo);
            } else if (taskDBInfo.getCurrentStatus() == State.SUCCESS
                    && FD_PackageUtil.isAppInstall(context, taskDBInfo.getPackageName())
                    && ((taskDBInfo.getVersionCode() != null)
                    && taskDBInfo.getVersionCode() == FD_PackageUtil.getVersionCode(context, taskDBInfo
                    .getPackageName()))) {
                taskDBInfo.setCurrentStatus(State.INSTALL);
                FD_FileUtil.deleteDownloadFile(context, taskDBInfo.getResKey(), taskDBInfo.getRangeNum());
                mDatabase.insertOrReplace(taskDBInfo);
            } else if (taskDBInfo.getCurrentStatus() == State.INSTALL && !FD_PackageUtil.isAppInstall(context,
                    taskDBInfo.getPackageName())) {
                mDatabase.delete(taskDBInfo);
            }
        }
        return true;
    }

    private List<TaskDBInfo> getTasks(int status) {
        return mDatabase.selectByStatus(status);
    }

    public synchronized TaskDBInfo getTaskDBInfoByResKey(String resKey) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectByResKey(resKey);
        if (taskDBInfos != null && !taskDBInfos.isEmpty()) {
            return taskDBInfos.get(0);
        } else {
            return null;
        }
    }

    public synchronized TaskDBInfo getTaskDBInfoByPackageName(String packageName) {
        List<TaskDBInfo> taskDBInfos = mDatabase.selectByPackageName(packageName);
        if (taskDBInfos != null && !taskDBInfos.isEmpty()) {
            return taskDBInfos.get(0);
        } else {
            return null;
        }
    }


    private TaskDBInfo taskInfo2TaskDBInfo(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        TaskDBInfo taskDBInfo = mTaskDBInfoContainer.get(resKey);
        if (taskDBInfo == null) {
            taskDBInfo = new TaskDBInfo();
            mTaskDBInfoContainer.put(resKey, taskDBInfo);
        } else {
            taskDBInfo.clear();
        }
        taskDBInfo.setResKey(taskInfo.getResKey());
        taskDBInfo.setUrl(taskInfo.getUrl());
        taskDBInfo.setFilePath(taskInfo.getFilePath());
        taskDBInfo.setCurrentSize(taskInfo.getCurrentSize());
        taskDBInfo.setCurrentStatus(taskInfo.getCurrentStatus());
        taskDBInfo.setTotalSize(taskInfo.getTotalSize());
        taskDBInfo.setRangeNum(taskInfo.getRangeNum());
        taskDBInfo.setPackageName(taskInfo.getPackageName());
        taskDBInfo.setTagClassName(taskInfo.getTagInfo().getTagClassName());
        taskDBInfo.setProgress(taskInfo.getProgress());
        taskDBInfo.setWifiAutoRetry(taskInfo.isWifiAutoRetry());
        taskDBInfo.setTagStr(taskInfo.getTagInfo().getTagStr());
        taskDBInfo.setTimeMillis(System.currentTimeMillis());
        taskDBInfo.setVersionCode(taskInfo.getVersionCode());
        taskDBInfo.setResponseCode(taskInfo.getResponseCode());
        return taskDBInfo;
    }
}
