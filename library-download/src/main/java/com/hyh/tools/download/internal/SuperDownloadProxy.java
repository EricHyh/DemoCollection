package com.hyh.tools.download.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import com.hyh.tools.download.api.CallbackAdapter;
import com.hyh.tools.download.api.HttpCall;
import com.hyh.tools.download.api.HttpClient;
import com.hyh.tools.download.bean.Command;
import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TaskInfo;

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

    private HttpCallFactory callFactory = new HttpCallFactory();

    private int maxSynchronousDownloadNum;

    private DownloadCallback downloadCallback;

    SuperDownloadProxy(Context context, int maxSynchronousDownloadNum) {
        this.context = context;
        this.client = getHttpClient();
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
        this.downloadCallback = new DownloadCallback();
    }

    @Override
    public void setMaxSynchronousDownloadNum(int maxSynchronousDownloadNum) {
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
    }

    private Map<String, AbstractHttpCallback> httpCallbacks = new ConcurrentHashMap<>();

    private List<TaskCache> waitingQueue = new CopyOnWriteArrayList<>();


    private AbstractHttpCallback getHttpCallbackImpl(TaskInfo taskInfo) {
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum > 1) {
            return new MultiHttpCallbackImpl(context, client, taskInfo, downloadCallback);
        } else {
            return new SingleHttpCallbackImpl(context, client, taskInfo, downloadCallback);
        }
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
                AbstractHttpCallback httpCallbackImpl = httpCallbacks.get(resKey);
                TaskInfo tTaskInfo = null;
                if (httpCallbackImpl != null) {
                    tTaskInfo = httpCallbackImpl.getTaskInfo();
                }
                if (tTaskInfo != null && !State.canDownload(tTaskInfo.getCurrentStatus())) {//检查状态
                    return;
                }
                if (runningTasksize >= maxSynchronousDownloadNum) {
                    handleWaitingInQueue(taskInfo);
                    return;
                }
                handlePrepare(taskInfo);
                HttpCall call = callFactory.produce(client, taskInfo);
                if (call == null) {
                    handleFailure(taskInfo);
                    return;
                }
                httpCallbackImpl = getHttpCallbackImpl(taskInfo);
                httpCallbacks.put(resKey, httpCallbackImpl);
                call.enqueue(httpCallbackImpl);
                break;
            case Command.PAUSE:
                AbstractHttpCallback remove = httpCallbacks.remove(resKey);
                if (remove != null) {
                    remove.pause();
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
                    remove.delete();
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
        Log.d("FDL_HH", "startNextTask start");
        if (!waitingQueue.isEmpty()) {
            TaskCache remove = waitingQueue.remove(0);
            Log.d("FDL_HH", "startNextTask resKey=" + remove.taskInfo.getResKey());
            enqueue(Command.START, remove.taskInfo);
        } else {
            if (httpCallbacks.isEmpty()) {
                //TODO 没任务了
                handleHaveNoTask();
                Log.d("FDL_HH", "startNextTask: 没任务了");
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
        Utils.deleteDownloadFile(context, taskInfo.getResKey(), taskInfo.getRangeNum());
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
