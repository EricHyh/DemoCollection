package com.hyh.tools.download.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.internal.db.bean.TaskDBInfo;
import com.hyh.tools.download.internal.db.dao.DaoMaster;
import com.hyh.tools.download.internal.db.dao.DaoSession;
import com.hyh.tools.download.internal.db.dao.TaskDBInfoDao;

import org.greenrobot.greendao.query.Query;

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

public class DBUtil {

    private static volatile DBUtil sDBUtil;

    private TaskDBInfoDao mDao;

    private Executor mExecutor;

    private final ConcurrentHashMap<String, TaskDBInfo> mTaskDBInfoContainer = new ConcurrentHashMap<>();

    public static DBUtil getInstance(Context context) {
        if (sDBUtil != null) {
            return sDBUtil;
        }
        synchronized (DBUtil.class) {
            if (sDBUtil == null) {
                sDBUtil = new DBUtil(context);
            }
            return sDBUtil;
        }
    }

    private DBUtil(Context context) {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context.getApplicationContext(), "taskdb", null);
        SQLiteDatabase db = devOpenHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        DaoSession session = master.newSession();
        this.mDao = session.getTaskDBInfoDao();
        this.mExecutor = Utils.buildExecutor(1, 1, 120, "Database Thread", true);
    }


    public void operate(final TaskInfo taskInfo) {
        if (taskInfo.getCurrentStatus() == State.DELETE || taskInfo.getCurrentStatus() == State.UNINSTALL) {
            delete(taskInfo);
        } else {
            insertOrReplace(taskInfo);
        }
    }


    private void insertOrReplace(final TaskInfo taskInfo) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TaskDBInfo newTaskDBInfo = taskInfo2TaskDBInfo(taskInfo);
                mDao.insertOrReplace(newTaskDBInfo);
            }
        });
    }

    public synchronized void insertOrReplace(TaskDBInfo taskDBInfo) {
        mDao.insertOrReplace(taskDBInfo);
    }


    private void delete(final TaskInfo taskInfo) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TaskDBInfo newTaskDBInfo = taskInfo2TaskDBInfo(taskInfo);
                Query<TaskDBInfo> query = mDao.queryBuilder()
                        .where(TaskDBInfoDao.Properties.ResKey.eq(newTaskDBInfo.getResKey()))
                        .orderDesc(TaskDBInfoDao.Properties.Time)
                        .build();
                if (query != null && query.list().size() > 0) {
                    mDao.delete(query.list().get(0));
                }
            }
        });
    }

    public synchronized void delete(final TaskDBInfo taskDBInfo) {
        mDao.delete(taskDBInfo);
    }


    public synchronized List<String> getSuccessList() {
        List<String> list = new ArrayList<>();
        Query<TaskDBInfo> query = mDao.queryBuilder()
                .where(TaskDBInfoDao.Properties.CurrentStatus.eq(State.SUCCESS))
                    /*.whereOr(TaskDBInfoDao.Properties.CurrentStatus.eq(State.SUCCESS), TaskDBInfoDao.Properties.CurrentStatus.eq(INSTALL))*/
                .build();
        if (query != null && query.list() != null) {
            List<TaskDBInfo> taskDBInfos = query.list();
            for (TaskDBInfo taskDBInfo : taskDBInfos) {
                list.add(taskDBInfo.getResKey());
            }
        }
        return list;
    }


    public synchronized Map<String, TaskDBInfo> getAllTaskMap() {
        Map<String, TaskDBInfo> historyTasks = new ConcurrentHashMap<>();
        List<TaskDBInfo> taskDBInfos = mDao.loadAll();
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
        List<TaskDBInfo> taskDBInfos = mDao.loadAll();
        return taskDBInfos == null ? new ArrayList<TaskDBInfo>() : taskDBInfos;
    }

    public synchronized boolean reviseDateBaseErroStatus(Context context) {
        List<TaskDBInfo> taskDBInfos = mDao.loadAll();
        if (taskDBInfos.isEmpty()) {
            return true;
        }
        for (TaskDBInfo taskDBInfo : taskDBInfos) {
            File file = new File(taskDBInfo.getFilePath());
            if (!file.exists()) {//可能是文件被删除了
                if (TextUtils.isEmpty(taskDBInfo.getPackageName())
                        || !PackageUtil.isAppInstall(context, taskDBInfo.getPackageName())) {//不是因为应用安装后删除文件的情况
                    Long currentSize = taskDBInfo.getCurrentSize();
                    if (currentSize != null && currentSize > 0) {
                        taskDBInfo.setCurrentSize(0L);
                        taskDBInfo.setProgress(0);
                        taskDBInfo.setCurrentStatus(State.PAUSE);
                        mDao.insertOrReplace(taskDBInfo);
                        continue;
                    }
                }
            }
            if (taskDBInfo.getCurrentStatus() == State.PREPARE || taskDBInfo.getCurrentStatus() == State.WAITING_IN_QUEUE
                    || taskDBInfo.getCurrentStatus() == State.START_WRITE || taskDBInfo.getCurrentStatus() == State.DOWNLOADING) {
                taskDBInfo.setCurrentStatus(State.PAUSE);
                mDao.insertOrReplace(taskDBInfo);
            } else if (taskDBInfo.getCurrentStatus() == State.SUCCESS
                    && PackageUtil.isAppInstall(context, taskDBInfo.getPackageName())
                    && ((taskDBInfo.getVersionCode() != null)
                    && taskDBInfo.getVersionCode() == PackageUtil.getVersionCode(context, taskDBInfo
                    .getPackageName()))) {
                taskDBInfo.setCurrentStatus(State.INSTALL);
                DownloadFileUtil.deleteDownloadFile(context, taskDBInfo.getResKey(), taskDBInfo.getRangeNum());
                mDao.insertOrReplace(taskDBInfo);
            } else if (taskDBInfo.getCurrentStatus() == State.INSTALL && !PackageUtil.isAppInstall(context,
                    taskDBInfo.getPackageName())) {
                mDao.delete(taskDBInfo);
            }
        }
        return true;
    }

    private List<TaskDBInfo> getTasks(int status) {
        Query<TaskDBInfo> query = mDao.queryBuilder()
                .where(TaskDBInfoDao.Properties.CurrentStatus.eq(status))
                .orderDesc(TaskDBInfoDao.Properties.Time)
                .build();
        if (query != null && query.list() != null) {
            return query.list();
        }
        return new ArrayList<>();
    }

    public synchronized TaskDBInfo getTaskDBInfoByResKey(String resKey) {
        Query<TaskDBInfo> query = mDao.queryBuilder().where(TaskDBInfoDao.Properties.ResKey.eq(resKey)).build();
        if (query != null && query.list() != null && query.list().size() > 0) {
            return query.list().get(0);
        } else {
            return null;
        }
    }

    public synchronized TaskDBInfo getTaskDBInfoByPackageName(String packageName) {
        Query<TaskDBInfo> query = mDao.queryBuilder().where(TaskDBInfoDao.Properties.PackageName.eq(packageName)).build();
        if (query != null && query.list() != null && query.list().size() > 0) {
            return query.list().get(0);
        } else {
            return null;
        }
    }


    public synchronized boolean isSuccessOrInstall(String resKey) {
        Query<TaskDBInfo> query = mDao.queryBuilder()
                .where(TaskDBInfoDao.Properties.ResKey.eq(resKey))
                .whereOr(TaskDBInfoDao.Properties.CurrentStatus.eq(State.SUCCESS),
                        TaskDBInfoDao.Properties.CurrentStatus.eq(State.INSTALL))
                .build();
        if (query == null) {
            return false;
        } else if (query.list() == null || query.list().size() == 0) {
            return false;
        } else {
            return true;
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
        taskDBInfo.setExpand(taskInfo.getExpand());
        taskDBInfo.setCurrentSize(taskInfo.getCurrentSize());
        taskDBInfo.setCurrentStatus(taskInfo.getCurrentStatus());
        taskDBInfo.setTotalSize(taskInfo.getTotalSize());
        taskDBInfo.setRangeNum(taskInfo.getRangeNum());
        taskDBInfo.setPackageName(taskInfo.getPackageName());
        taskDBInfo.setTagClassName(taskInfo.getTagClassName());
        taskDBInfo.setProgress(taskInfo.getProgress());
        taskDBInfo.setWifiAutoRetry(taskInfo.isWifiAutoRetry());
        taskDBInfo.setTagJson(taskInfo.getTagJson());
        taskDBInfo.setTime(System.currentTimeMillis());
        taskDBInfo.setVersionCode(taskInfo.getVersionCode());
        taskDBInfo.setResponseCode(taskInfo.getCode());
        return taskDBInfo;
    }
}
