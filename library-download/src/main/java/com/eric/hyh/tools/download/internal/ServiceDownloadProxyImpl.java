package com.eric.hyh.tools.download.internal;

import android.content.Context;
import android.os.RemoteException;

import com.eric.hyh.tools.download.IClient;
import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public abstract class ServiceDownloadProxyImpl extends SuperDownloadProxy implements IDownloadProxy.IServiceDownloadProxy {

    private Map<Integer, IClient> mClients;
    private Executor mExecutor;
    private Map<String, TaskDBInfo> mTaskDBInfoContainer;

    ServiceDownloadProxyImpl(Context context, Map<Integer, IClient> clients, Executor executor, Map<String, TaskDBInfo> taskDBInfoContainer) {
        super(context);
        this.mClients = clients;
        this.mExecutor = executor;
        if (taskDBInfoContainer != null) {
            this.mTaskDBInfoContainer = taskDBInfoContainer;
        } else {
            this.mTaskDBInfoContainer = new ConcurrentHashMap<>();
        }
    }

    @Override
    protected void handleHaveNoTask() {
        Collection<IClient> values = mClients.values();
        try {
            for (IClient value : values) {
                value.onHaveNoTask();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleCallbackAndDB(TaskInfo taskInfo, Callback... callback) {
        Collection<IClient> values = mClients.values();
        try {
            for (IClient value : values) {
                value.onCall(taskInfo);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
