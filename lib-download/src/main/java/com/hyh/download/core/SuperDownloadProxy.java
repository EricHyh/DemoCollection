package com.hyh.download.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hyh.download.CallbackAdapter;
import com.hyh.download.Command;
import com.hyh.download.State;
import com.hyh.download.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.ntv.NativeHttpClient;
import com.hyh.download.utils.DownloadFileHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public abstract class SuperDownloadProxy implements IDownloadProxy {

    protected Context context;

    private HttpClient client;

    private HttpCallFactory callFactory = new HttpCallFactory();

    private Map<String, AbstractHttpCallback> httpCallbackMap = new ConcurrentHashMap<>();

    private Map<String, TaskCache> waitingQueue = new ConcurrentHashMap<>();

    private int maxSynchronousDownloadNum;

    private DownloadCallback downloadCallback;

    private Map<String, Integer> mTaskCommandMap = new ConcurrentHashMap<>();

    SuperDownloadProxy(Context context, int maxSynchronousDownloadNum) {
        this.context = context;
        this.client = new NativeHttpClient(context);
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
        this.downloadCallback = new DownloadCallback();
    }

    @Override
    public void setMaxSynchronousDownloadNum(int maxSynchronousDownloadNum) {
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
    }

    private AbstractHttpCallback getHttpCallbackImpl(TaskInfo taskInfo) {
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum > 1) {
            return new MultiHttpCallbackImpl(context, client, taskInfo, downloadCallback);
        } else {
            return new SingleHttpCallbackImpl(context, client, taskInfo, downloadCallback);
        }
    }

    @Override
    public void onReceiveStartCommand(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mTaskCommandMap.put(resKey, Command.START);
    }

    @Override
    public void onReceivePauseCommand(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mTaskCommandMap.put(resKey, Command.PAUSE);
    }

    @Override
    public void onReceiveDeleteCommand(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mTaskCommandMap.put(resKey, Command.DELETE);
    }


    @Override
    public boolean isFileDownloading(String resKey) {
        return false;
    }

    @Override
    public boolean isFileDownloaded(String resKey) {
        return false;
    }

    @Override
    public TaskInfo getTaskInfoByKey(String resKey) {
        return null;
    }

    @Override
    public void deleteTask(String resKey) {

    }

    @Override
    public synchronized void enqueue(int command, TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        Integer integer = mTaskCommandMap.get(resKey);
        if (integer == null || integer != command) {
            return;
        }
        switch (command) {
            case Command.START:
                int runningTaskSize = httpCallbackMap.size();
                AbstractHttpCallback httpCallbackImpl = httpCallbackMap.get(resKey);
                TaskInfo cacheTaskInfo = null;
                if (httpCallbackImpl != null) {
                    cacheTaskInfo = httpCallbackImpl.getTaskInfo();
                }
                if (cacheTaskInfo != null) {//检查状态
                    if (!canDownload(cacheTaskInfo.getCurrentStatus())) {
                        return;
                    }
                }
                if (runningTaskSize >= maxSynchronousDownloadNum) {
                    handleWaitingInQueue(taskInfo);
                    return;
                }
                HttpCall call = callFactory.produce(client, taskInfo);
                if (call == null) {
                    handleFailure(taskInfo);
                    return;
                }
                httpCallbackImpl = getHttpCallbackImpl(taskInfo);
                httpCallbackMap.put(resKey, httpCallbackImpl);
                call.enqueue(httpCallbackImpl);
                break;
            case Command.PAUSE:
                AbstractHttpCallback remove = httpCallbackMap.remove(resKey);
                if (remove != null) {
                    remove.pause();
                    handlePause(taskInfo);
                } else {
                    TaskCache taskCache = null;
                    for (TaskCache task : waitingQueue.values()) {
                        if (TextUtils.equals(task.resKey, resKey)) {
                            taskCache = task;
                            break;
                        }
                    }
                    if (taskCache != null) {
                        waitingQueue.remove(resKey);
                    }
                    handlePause(taskInfo);
                }
                break;
            case Command.DELETE:
                remove = httpCallbackMap.remove(resKey);
                if (remove != null) {
                    remove.delete();
                    handleDelete(taskInfo);
                } else {
                    TaskCache taskCache = null;
                    for (TaskCache task : waitingQueue.values()) {
                        if (TextUtils.equals(task.resKey, resKey)) {
                            taskCache = task;
                            break;
                        }
                    }
                    if (taskCache != null) {
                        waitingQueue.remove(resKey);
                    }
                    handleDelete(taskInfo);
                }
                break;
        }
    }


    private boolean canDownload(int currentStatus) {
        if (currentStatus == State.PREPARE
                || currentStatus == State.START_WRITE
                || currentStatus == State.DOWNLOADING
                || currentStatus == State.WAITING_IN_QUEUE
                || currentStatus == State.SUCCESS) {
            return false;
        }
        return true;
    }

    private synchronized void startNextTask() {
        Log.d("FDL_HH", "startNextTask start");
        if (!waitingQueue.isEmpty()) {
            TaskCache remove = waitingQueue.remove(0);
            Log.d("FDL_HH", "startNextTask resKey=" + remove.taskInfo.getResKey());
            enqueue(Command.START, remove.taskInfo);
        } else {
            if (httpCallbackMap.isEmpty()) {
                handleHaveNoTask();
                Log.d("FDL_HH", "startNextTask: 没任务了");
            }
        }
    }

    private void handleFirstFileWrite(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.START_WRITE);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }

    private void handlePause(TaskInfo taskInfo) {
        startNextTask();
    }

    private void handleDelete(TaskInfo taskInfo) {
        DownloadFileHelper.deleteDownloadFile(taskInfo);
        startNextTask();
    }


    private void handleWaitingInQueue(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        taskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);
        TaskCache taskCache = new TaskCache(resKey, taskInfo);
        waitingQueue.remove(taskInfo.getResKey());
        waitingQueue.put(resKey, taskCache);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }


    private void handleSuccess(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.SUCCESS);
        String resKey = taskInfo.getResKey();
        httpCallbackMap.remove(resKey);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
        startNextTask();
    }

    private void handleFailure(TaskInfo taskInfo) {
        if (taskInfo.isWifiAutoRetryFailedTask()) {
            taskInfo.setCurrentStatus(State.WAITING_FOR_WIFI);
        } else {
            taskInfo.setCurrentStatus(State.FAILURE);
        }
        String resKey = taskInfo.getResKey();
        httpCallbackMap.remove(resKey);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
        startNextTask();
    }


    private void handleDownloading(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.DOWNLOADING);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }

    protected abstract void handleHaveNoTask();

    protected abstract void handleCallback(TaskInfo taskInfo);

    protected abstract void handleDatabase(TaskInfo taskInfo);

    @Override
    public boolean isTaskEnqueue(String resKey) {
        return waitingQueue.containsKey(resKey) || httpCallbackMap.containsKey(resKey);
    }

    private class DownloadCallback extends CallbackAdapter {
        @Override
        public void onFirstFileWrite(TaskInfo taskInfo) {
            handleFirstFileWrite(taskInfo);
        }

        @Override
        public void onDownloading(TaskInfo taskInfo) {
            handleDownloading(taskInfo);
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
