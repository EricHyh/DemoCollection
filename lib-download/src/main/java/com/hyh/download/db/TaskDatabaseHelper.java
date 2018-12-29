package com.hyh.download.db;

import android.content.Context;

import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.db.dao.TaskInfoDao;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.RangeUtil;
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
                fixTaskInfo(taskInfo);
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
                mTaskInfoMap.put(taskInfo.getResKey(), taskInfo);
            }
        }
        if (taskInfo != null) {
            fixTaskInfo(taskInfo);
        }
        return taskInfo;
    }

    private void fixTaskInfo(TaskInfo taskInfo) {
        int currentStatus = taskInfo.getCurrentStatus();
        if (currentStatus == State.SUCCESS) {
            long fileLength = DownloadFileHelper.getFileLength(taskInfo.getFilePath());
            long totalSize = taskInfo.getTotalSize();
            if (fileLength <= 0 || (totalSize > 0 && fileLength != totalSize)) {
                DownloadFileHelper.deleteDownloadFile(taskInfo);
            } else {
                taskInfo.setCurrentSize(fileLength);
                taskInfo.setProgress(RangeUtil.computeProgress(fileLength, totalSize));
            }
        } else {
            if (taskInfo.getRangeNum() > 1) {
                String filePath = taskInfo.getFilePath();
                String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
                long currentSize = readCurrentSizeFromTempFile(tempFilePath, taskInfo.getTotalSize(), taskInfo.getRangeNum());
                taskInfo.setCurrentSize(currentSize);
                long totalSize = taskInfo.getTotalSize();
                taskInfo.setProgress(RangeUtil.computeProgress(currentSize, totalSize));
            } else {
                String filePath = taskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                taskInfo.setCurrentSize(fileLength);
                long totalSize = taskInfo.getTotalSize();
                taskInfo.setProgress(RangeUtil.computeProgress(fileLength, totalSize));
            }
        }
    }


    private long readCurrentSizeFromTempFile(String tempFilePath, long totalSize, int rangeNum) {
        if (totalSize == 0) {
            return 0;
        }
        long[] originalStartPositions = RangeUtil.computeStartPositions(totalSize, rangeNum);
        long currentSize = 0;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tempFilePath, "rw");
            for (int index = 0; index < rangeNum; index++) {
                raf.seek(index * 8);
                long startPosition = raf.readLong();
                currentSize += (startPosition - originalStartPositions[index]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(raf);
        }
        return currentSize;
    }
}

