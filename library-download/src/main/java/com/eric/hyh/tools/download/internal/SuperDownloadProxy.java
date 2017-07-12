package com.eric.hyh.tools.download.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import com.eric.hyh.tools.download.api.CallbackAdapter;
import com.eric.hyh.tools.download.api.HttpCall;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public abstract class SuperDownloadProxy implements IDownloadProxy {

    protected Context context;

    private HttpClient client;

    private int maxSynchronousDownloadNum;

    private int getTotalSizeRetryTimes;

    SuperDownloadProxy(Context context, int maxSynchronousDownloadNum) {
        this.context = context;
        this.client = getHttpClient();
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
    }

    @Override
    public void setMaxSynchronousDownloadNum(int maxSynchronousDownloadNum) {
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
    }

    private Map<String, HttpCallbackImpl> httpCallbacks = new ConcurrentHashMap<>();

    private List<TaskCache> waitingQueue = new CopyOnWriteArrayList<>();


    private HttpCall getCall(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        String url = taskInfo.getUrl();
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum <= 1) {
            return client.newCall(resKey, url, taskInfo.getCurrentSize());
        } else {
            long totalSize = getTotalSize(taskInfo);
            if (totalSize > 0) {
                long[] startPositions = taskInfo.getStartPositions();
                if (startPositions == null) {
                    startPositions = getStartPositions(totalSize, rangeNum);
                }
                taskInfo.setStartPositions(startPositions);
                long[] endPositions = getEndPositions(totalSize, rangeNum);
                Map<String, HttpCall> httpCallMap = new HashMap<>();
                for (int index = 0; index < rangeNum; index++) {
                    long startPosition = startPositions[index];
                    long endPosition = endPositions[index];
                    if (startPosition < endPosition) {
                        String tag = resKey.concat("-").concat(String.valueOf(index));
                        httpCallMap.put(tag, client.newCall(tag, url, startPosition, endPosition));
                    }
                }
                return new MultiHttpCall(httpCallMap);
            }
        }
        return null;
    }

    private long getTotalSize(TaskInfo taskInfo) {
        long totalSize = taskInfo.getTotalSize();
        if (totalSize == 0) {
            try {
                totalSize = client.getContentLength(taskInfo.getUrl());
                if (totalSize > 0) {
                    taskInfo.setTotalSize(totalSize);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (getTotalSizeRetryTimes++ < 3) {
                    return getTotalSize(taskInfo);
                }
            }
        }
        return totalSize;
    }

    private long[] getStartPositions(long totalSize, int rangeNum) {
        long[] startPositions = new long[rangeNum];
        long rangeSize = rangeNum / totalSize;
        for (int index = 0; index < rangeNum; index++) {
            if (index == 0) {
                startPositions[index] = 0;
            } else {
                startPositions[index] = rangeSize * index + 1;
            }

        }
        return startPositions;
    }

    private long[] getEndPositions(long totalSize, int rangeNum) {
        long[] endPositions = new long[rangeNum];
        long rangeSize = rangeNum / totalSize;
        for (int index = 0; index < rangeNum; index++) {
            if (index < rangeNum - 1) {
                endPositions[index] = rangeSize * (index + 1);
            } else {
                endPositions[index] = totalSize;
            }
        }
        return endPositions;
    }


    private HttpCallbackImpl getHttpCallbackImpl(TaskInfo taskInfo) {


        return new HttpCallbackImpl(context, client, taskInfo, new DownloadCallback());
    }

    protected abstract HttpClient getHttpClient();

    @SuppressWarnings("unchecked")
    @Override
    public void enqueue(int command, TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        switch (command) {
            case Command.START:
            case Command.UPDATE:
                int runningTasksize = httpCallbacks.size();
                HttpCallbackImpl httpCallbackImpl = httpCallbacks.get(resKey);
                TaskInfo tTaskInfo = null;
                if (httpCallbackImpl != null) {
                    tTaskInfo = httpCallbackImpl.taskInfo;
                }
                if (tTaskInfo != null && !State.canDownload(tTaskInfo.getCurrentStatus())) {//检查状态
                    return;
                }
                if (runningTasksize >= maxSynchronousDownloadNum) {
                    handleWaitingInQueue(taskInfo);
                    return;
                }
                handlePrepare(taskInfo);
                HttpCall call = getCall(taskInfo);
                if (call == null) {
                    handleFailure(taskInfo);
                    return;
                }
                httpCallbackImpl = getHttpCallbackImpl(taskInfo);
                httpCallbacks.put(resKey, httpCallbackImpl);
                call.enqueue(httpCallbackImpl);
                break;
            case Command.PAUSE:
                HttpCallbackImpl remove = httpCallbacks.remove(resKey);
                if (remove != null) {
                    remove.setPause(true);
                    if (remove.call != null && !remove.call.isCanceled()) {
                        remove.call.cancel();
                    }
                    handlePause(taskInfo);
                } else {
                    TaskCache taskCache = null;
                    for (TaskCache task : waitingQueue) {
                        if (TextUtils.equals(task.resKey, resKey)) {
                            taskCache = task;
                            break;
                        }
                    }
                    if (taskCache != null) {
                        waitingQueue.remove(taskCache);
                    }
                    handlePause(taskInfo);
                }
                break;
            case Command.DELETE:
                remove = httpCallbacks.remove(resKey);
                if (remove != null) {
                    remove.setDelete(true);
                    if (remove.call != null && !remove.call.isCanceled()) {
                        remove.call.cancel();
                    }
                    handleDelete(taskInfo);
                } else {
                    TaskCache taskCache = null;
                    for (TaskCache task : waitingQueue) {
                        if (TextUtils.equals(task.resKey, resKey)) {
                            taskCache = task;
                            break;
                        }
                    }
                    if (taskCache != null) {
                        waitingQueue.remove(taskCache);
                    }
                    handleDelete(taskInfo);
                }
                break;
        }
    }

    private void startNextTask() {
        if (!waitingQueue.isEmpty()) {
            TaskCache remove = waitingQueue.remove(0);
            enqueue(Command.START, remove.taskInfo);
        } else {
            if (httpCallbacks.isEmpty()) {
                //TODO 没任务了
                handleHaveNoTask();
                Log.d("DownloadModule", "startNextTask: 没任务了");
            }
        }
    }


    private void handleWaitingInQueue(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);
        TaskCache taskCache = new TaskCache(taskInfo.getResKey(), taskInfo);
        waitingQueue.remove(taskCache);
        waitingQueue.add(taskCache);
        handleCallbackAndDB(taskInfo);
    }


    private void handleSuccess(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.SUCCESS);
        String resKey = taskInfo.getResKey();
        httpCallbacks.remove(resKey);
        if (TextUtils.isEmpty(taskInfo.getPackageName())) {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(taskInfo.getFilePath(), 0);
            if (packageInfo != null) {
                taskInfo.setPackageName(packageInfo.packageName);
            }
        }
        handleCallbackAndDB(taskInfo);
        startNextTask();
    }


    private void handleDelete(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.DELETE);
        handleCallbackAndDB(taskInfo);
        Utils.deleteDownloadFile(context, taskInfo.getResKey());
        startNextTask();
    }

    private void handlePause(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.PAUSE);
        handleCallbackAndDB(taskInfo);
        startNextTask();
    }


    private void handlePrepare(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.PREPARE);
        handleCallbackAndDB(taskInfo);
    }

    private void handleFirstFileWrite(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.START_WRITE);
        handleCallbackAndDB(taskInfo);
    }

    private void handleFailure(TaskInfo taskInfo) {
        if (taskInfo.isWifiAutoRetry()) {
            taskInfo.setCurrentStatus(State.WAITING_FOR_WIFI);
        } else {
            taskInfo.setCurrentStatus(State.FAILURE);
        }
        String resKey = taskInfo.getResKey();
        httpCallbacks.remove(resKey);
        handleCallbackAndDB(taskInfo);
        startNextTask();
    }


    private void handleDownloading(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.DOWNLOADING);
        handleCallbackAndDB(taskInfo);
    }

    protected abstract void handleHaveNoTask();


    protected abstract void handleCallbackAndDB(TaskInfo taskInfo);


    private class DownloadCallback extends CallbackAdapter {
        @Override
        public void onFirstFileWrite(TaskInfo taskInfo) {
            handleFirstFileWrite(taskInfo);
        }

        @Override
        public void onDelete(TaskInfo taskInfo) {
            handleDelete(taskInfo);
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            handlePause(taskInfo);
        }

        @Override
        public void onFailure(TaskInfo taskInfo) {
            handleFailure(taskInfo);
        }

        @Override
        public void onSuccess(TaskInfo taskInfo) {
            handleSuccess(taskInfo);
        }
    }
}
