package com.hyh.tools.download.internal;

import android.content.Context;
import android.os.RemoteException;

import com.hyh.tools.download.IClient;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.utils.FD_DBUtil;

import java.util.Collection;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class ServiceDownloadProxyImpl extends SuperDownloadProxy implements IDownloadProxy.IServiceDownloadProxy {

    private Map<Integer, IClient> mClients;

    ServiceDownloadProxyImpl(Context context, Map<Integer, IClient> clients,
                             int maxSynchronousDownloadNum) {
        super(context, maxSynchronousDownloadNum);
        this.mClients = clients;
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
    protected void handleCallbackAndDB(TaskInfo taskInfo) {
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
        FD_DBUtil.getInstance(context).operate(taskInfo);
    }
}
