package com.hyh.download.db;

import android.content.Context;
import android.os.Looper;

import com.hyh.download.FileRequest;
import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.db.dao.TaskInfoDao;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.ProgressHelper;

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

    public void fixDatabaseErrorStatus() {
        List<TaskInfo> taskInfoList = mTaskInfoDao.loadAll();
        List<FileRequest> interruptRequests = null;
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
                        if (totalSize > 0) {
                            isRealSuccess = fileLength == totalSize;
                        } else {
                            isRealSuccess = true;
                        }
                    }
                    if (!isRealSuccess) {
                        taskInfo.setCurrentSize(fileLength);
                        taskInfo.setProgress(ProgressHelper.computeProgress(fileLength, totalSize));
                        taskInfo.setCurrentStatus(State.FAILURE);
                        insertOrUpdate(taskInfo);
                    }
                    mTaskInfoMap.put(resKey, taskInfo);
                    continue;
                }
                taskInfo.setCurrentStatus(State.FAILURE);
                insertOrUpdate(taskInfo);
                mTaskInfoMap.put(resKey, taskInfo);
                if (taskInfo.isPermitRetryIfInterrupt()) {
                    if (interruptRequests == null) {
                        interruptRequests = new ArrayList<>();
                    }
                    interruptRequests.add(taskInfo.toFileRequest());
                }
            }
        }
        if (interruptRequests != null) {
            final List<FileRequest> finalInterruptRequests = interruptRequests;
            new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (FileRequest fileRequest : finalInterruptRequests) {

                    }
                }
            });
        }
    }

    public void insertOrUpdate(final TaskInfo taskInfo) {
        mTaskInfoDao.insertOrUpdate(taskInfo);
    }

    public void delete(String resKey) {
        mTaskInfoDao.delete(resKey);
    }

    public void delete(TaskInfo taskInfo) {
        mTaskInfoDao.delete(taskInfo);
    }

    public Map<String, TaskInfo> getAllTask() {
        return null;
    }

    public TaskInfo getTaskInfoByKey(String resKey) {
        return null;
    }
}

