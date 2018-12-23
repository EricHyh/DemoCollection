package com.hyh.download.db;

import android.content.Context;
import android.os.RemoteException;

import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.db.dao.TaskInfoDao;
import com.hyh.download.utils.DownloadFileHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class TaskDatabaseHelper {


    private static final TaskDatabaseHelper sInstance = new TaskDatabaseHelper();

    private TaskInfoDao mTaskInfoDao;

    private Map<String, TaskInfo> mTaskInfoMap = new ConcurrentHashMap<>();

    public static TaskDatabaseHelper getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mTaskInfoDao = new TaskInfoDao(context);
    }

    public List<TaskInfo> fixDatabaseErrorStatus(IFileChecker globalFileChecker) {
        List<TaskInfo> taskInfoList = mTaskInfoDao.loadAll();
        List<TaskInfo> interruptTasks = null;
        if (taskInfoList != null && !taskInfoList.isEmpty()) {
            for (TaskInfo taskInfo : taskInfoList) {
                String resKey = taskInfo.getResKey();
                int currentStatus = taskInfo.getCurrentStatus();
                if (currentStatus == State.DELETE
                        || currentStatus == State.PAUSE
                        || currentStatus == State.NONE
                        || currentStatus == State.WAITING_FOR_WIFI
                        || currentStatus == State.FAILURE) {
                    mTaskInfoMap.put(resKey, taskInfo);
                    continue;
                }
                if (currentStatus == State.SUCCESS) {
                    String filePath = taskInfo.getFilePath();
                    long totalSize = taskInfo.getTotalSize();
                    long fileLength = DownloadFileHelper.getFileLength(filePath);
                    boolean isRealSuccess = false;
                    if (fileLength > 0) {
                        isRealSuccess = totalSize <= 0 || fileLength == totalSize;
                    }
                    if (isRealSuccess && globalFileChecker != null) {
                        if (!checkFile(taskInfo, globalFileChecker)) {
                            isRealSuccess = false;
                        }
                    }
                    if (!isRealSuccess) {
                        DownloadFileHelper.deleteDownloadFile(taskInfo);
                        taskInfo.setTotalSize(0);
                        taskInfo.setCurrentSize(0);
                        taskInfo.setProgress(0);
                        taskInfo.setETag(null);
                        taskInfo.setCurrentStatus(State.NONE);
                        insertOrUpdate(taskInfo);
                    }
                    mTaskInfoMap.put(resKey, taskInfo);
                    continue;
                }
                taskInfo.setCurrentStatus(State.FAILURE);
                insertOrUpdate(taskInfo);
                mTaskInfoMap.put(resKey, taskInfo);
                if (taskInfo.isPermitRecoverTask()) {
                    if (interruptTasks == null) {
                        interruptTasks = new ArrayList<>();
                    }
                    interruptTasks.add(taskInfo);
                }
            }
        }
        return interruptTasks;
    }

    private boolean checkFile(TaskInfo taskInfo, IFileChecker globalFileChecker) {
        try {
            return globalFileChecker.isValidFile(taskInfo.toDownloadInfo());
        } catch (RemoteException e) {
            return true;
        }
    }

    public void insertOrUpdate(final TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mTaskInfoMap.put(resKey, taskInfo);
        mTaskInfoDao.insertOrUpdate(taskInfo);
    }

    public void delete(String resKey) {
        mTaskInfoMap.remove(resKey);
        mTaskInfoDao.delete(resKey);
    }

    public void delete(TaskInfo taskInfo) {
        delete(taskInfo.getResKey());
    }

    public TaskInfo getTaskInfoByKey(String resKey) {
        return mTaskInfoMap.get(resKey);
    }
}

