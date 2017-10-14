package com.hyh.tools.download.internal;

import android.content.Context;

import com.hyh.tools.download.api.Callback;
import com.hyh.tools.download.api.FileDownloader;
import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class LocalDownloadProxyImpl extends SuperDownloadProxy implements IDownloadProxy.ILocalDownloadProxy {

    private Callback mCallback;
    private final Utils.DBUtil mDBUtil;
    private ConcurrentHashMap<String, TaskDBInfo> mTaskDBInfoContainer;
    private ThreadPoolExecutor mDatabaseExecutor;

    public LocalDownloadProxyImpl(Context context, int maxSynchronousDownloadNum) {
        super(context, maxSynchronousDownloadNum);
        mDBUtil = Utils.DBUtil.getInstance(context);
        mTaskDBInfoContainer = new ConcurrentHashMap<>();
        mDatabaseExecutor = Utils.buildExecutor(1, 1, 120, "FDLService Database Thread", true);

    }


    @Override
    public void initProxy(final FileDownloader.LockConfig lockConfig) {
        mDatabaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDBUtil.correctDBErroStatus(context);
                synchronized (lockConfig) {
                    lockConfig.setInitProxyFinish(true);
                    lockConfig.notifyAll();
                }
            }
        });
    }

    @Override
    public boolean isOtherProcessDownloading(String resKey) {
        return false;
    }


    @Override
    protected void handleHaveNoTask() {
        if (mCallback != null) {
            mCallback.onHaveNoTask();
        }
    }

    @Override
    protected void handleCallbackAndDB(TaskInfo taskInfo) {
        if (mCallback != null) {
            handleCallback(taskInfo, mCallback);
        }
        handleDB(taskInfo);
    }

    @SuppressWarnings("unchecked")
    private void handleCallback(TaskInfo taskInfo, Callback callback) {
        switch (taskInfo.getCurrentStatus()) {
            case State.PREPARE:
                callback.onPrepare(taskInfo);
                break;
            case State.START_WRITE:
                callback.onFirstFileWrite(taskInfo);
                break;
            case State.DOWNLOADING:
                callback.onDownloading(taskInfo);
                break;
            case State.WAITING_IN_QUEUE:
                callback.onWaitingInQueue(taskInfo);
                break;
            case State.WAITING_FOR_WIFI:
                callback.onWaitingForWifi(taskInfo);
                break;
            case State.PAUSE:
                callback.onPause(taskInfo);
                break;
            case State.DELETE:
                callback.onDelete(taskInfo);
                break;
            case State.FAILURE:
                callback.onFailure(taskInfo);
                break;
            case State.SUCCESS:
                callback.onSuccess(taskInfo);
                break;
            case State.INSTALL:
                callback.onInstall(taskInfo);
                break;
            case State.UNINSTALL:
                callback.onUnInstall(taskInfo);
                break;
        }
    }

    private void handleDB(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        TaskDBInfo taskDBInfo = mTaskDBInfoContainer.get(resKey);
        if (taskDBInfo == null) {
            taskDBInfo = new TaskDBInfo();
            mTaskDBInfoContainer.put(resKey, taskDBInfo);
        }
        mDBUtil.operate(taskInfo, taskDBInfo, mDatabaseExecutor);
    }


    @Override
    public void operateDatebase(TaskInfo taskInfo) {
        handleDB(taskInfo);
    }

    @Override
    public void setAllTaskCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void destroy() {

    }
}
