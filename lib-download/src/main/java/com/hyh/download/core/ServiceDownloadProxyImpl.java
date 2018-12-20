package com.hyh.download.core;

import android.content.Context;
import android.os.RemoteException;

import com.hyh.download.IClient;
import com.hyh.download.db.bean.TaskInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2017/5/17
 */

public class ServiceDownloadProxyImpl extends SuperDownloadProxy implements IDownloadProxy {

    private Map<Integer, IClient> mClients;

    public ServiceDownloadProxyImpl(Context context, Map<Integer, IClient> clients, int maxSynchronousDownloadNum) {
        super(context, maxSynchronousDownloadNum);
        this.mClients = clients;
    }

    @Override
    protected void handleCallback(TaskInfo taskInfo) {
        Set<Map.Entry<Integer, IClient>> entries = mClients.entrySet();
        Iterator<Map.Entry<Integer, IClient>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, IClient> entry = iterator.next();
            IClient client = entry.getValue();
            try {
                client.onCallback(taskInfo.toDownloadInfo());
            } catch (RemoteException e) {
                if (!isAlive(client)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    protected void handleHaveNoTask() {
        Set<Map.Entry<Integer, IClient>> entries = mClients.entrySet();
        Iterator<Map.Entry<Integer, IClient>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, IClient> entry = iterator.next();
            IClient client = entry.getValue();
            try {
                client.onHaveNoTask();
            } catch (RemoteException e) {
                if (!isAlive(client)) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean isAlive(IClient client) {
        try {
            return client.isAlive();
        } catch (Exception e) {
            return false;
        }
    }
}
