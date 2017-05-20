package com.eric.hyh.tools.download.internal;

import android.content.Context;
import android.os.RemoteException;

import com.eric.hyh.tools.download.ICallback;
import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public abstract class ServiceDownloadAgentImpl extends SuperDownloadAgent implements IDownloadAgent.IServiceDownloadAgent {

    private ICallback mCallback;
    private Executor mExecutor;
    private Map<String, TaskDBInfo> mTaskDBInfoContainer;

    ServiceDownloadAgentImpl(Context context, Executor executor, Map<String, TaskDBInfo> taskDBInfoContainer) {
        super(context);
        this.mExecutor = executor;
        if (taskDBInfoContainer != null) {
            this.mTaskDBInfoContainer = taskDBInfoContainer;
        } else {
            this.mTaskDBInfoContainer = new ConcurrentHashMap<>();
        }
    }

    @Override
    public void setCallback(ICallback callback) {
        this.mCallback = callback;
    }


    @Override
    protected void handleHaveNoTask() {
        if (mCallback != null) {
            try {
                mCallback.onHaveNoTask();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void handleCallbackAndDB(TaskInfo taskInfo, Callback... callback) {
        if (mCallback != null) {
            try {
                mCallback.onCall(taskInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        handleDB(taskInfo);
    }

    private void handleDB(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        TaskDBInfo taskDBInfo = mTaskDBInfoContainer.get(resKey);
        if (taskDBInfo == null) {
            taskDBInfo = new TaskDBInfo();
            mTaskDBInfoContainer.put(resKey, taskDBInfo);
        }
        Utils.DBUtil.getInstance(context).operate(taskInfo, taskDBInfo, mExecutor);
    }
}
