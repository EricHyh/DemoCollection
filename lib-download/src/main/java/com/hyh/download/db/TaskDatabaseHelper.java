package com.hyh.download.db;

import android.content.Context;

import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.db.dao.TaskInfoDao;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.ProgressHelper;
import com.hyh.download.utils.StreamUtil;

import java.io.RandomAccessFile;
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

    public List<TaskInfo> getInterruptedTasks() {
        List<TaskInfo> taskInfoList = mTaskInfoDao.queryInterruptedTask();
        List<TaskInfo> interruptTasks = null;
        if (taskInfoList != null && !taskInfoList.isEmpty()) {
            for (TaskInfo taskInfo : taskInfoList) {
                fixCurrentSize(taskInfo);
                taskInfo.setCurrentStatus(State.FAILURE);
                insertOrUpdate(taskInfo);
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

    private void fixCurrentSize(TaskInfo taskInfo) {
        if (taskInfo.getRangeNum() > 1) {
            String filePath = taskInfo.getFilePath();
            String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
            long currentSize = readCurrentSizeFromTempFile(tempFilePath, taskInfo.getRangeNum());
            taskInfo.setCurrentSize(currentSize);
            long totalSize = taskInfo.getTotalSize();
            if (totalSize > 0) {
                taskInfo.setProgress(ProgressHelper.computeProgress(currentSize, totalSize));
            } else {
                taskInfo.setProgress(-1);
            }
        } else {
            String filePath = taskInfo.getFilePath();
            long fileLength = DownloadFileHelper.getFileLength(filePath);
            taskInfo.setCurrentSize(fileLength);
            long totalSize = taskInfo.getTotalSize();
            if (totalSize > 0) {
                taskInfo.setProgress(ProgressHelper.computeProgress(fileLength, totalSize));
            } else {
                taskInfo.setProgress(-1);
            }
        }
    }

    private long readCurrentSizeFromTempFile(String tempFilePath, int rangeNum) {
        long currentSize = 0;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tempFilePath, "rw");
            for (int index = 0; index < rangeNum; index++) {
                raf.seek(index * 8);
                currentSize += raf.readLong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(raf);
        }
        return currentSize;
    }

    public void insertOrUpdate(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mTaskInfoMap.put(resKey, taskInfo);
        mTaskInfoDao.insertOrUpdate(taskInfo);
    }

    public void updateCacheOnly(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mTaskInfoMap.put(resKey, taskInfo);
    }

    public void delete(String resKey) {
        mTaskInfoMap.remove(resKey);
        mTaskInfoDao.delete(resKey);
    }

    public void delete(TaskInfo taskInfo) {
        delete(taskInfo.getResKey());
    }

    public TaskInfo getTaskInfoByKey(String resKey) {
        TaskInfo taskInfo = mTaskInfoMap.get(resKey);
        if (taskInfo == null) {
            taskInfo = mTaskInfoDao.getTaskInfoByKey(resKey);
            if (taskInfo != null) {
                fixCurrentSize(taskInfo);
                mTaskInfoMap.put(taskInfo.getResKey(), taskInfo);
            }
        } else {
            long currentSize = taskInfo.getCurrentSize();
            L.d("getTaskInfoByKey currentSize = " + currentSize);
        }
        return taskInfo;
    }
}

