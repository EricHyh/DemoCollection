package com.hyh.download.core;

import android.os.RemoteException;

import com.hyh.download.DownloadInfo;
import com.hyh.download.ITaskListener;
import com.hyh.download.TaskListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Eric_He on 2019/1/6.
 */

public class ITaskListenerWrapper implements TaskListener {

    private final Map<Integer, ITaskListener> mTaskListenerMap;

    public ITaskListenerWrapper(Map<Integer, ITaskListener> taskListenerMap) {
        mTaskListenerMap = taskListenerMap;
    }

    @Override
    public void onPrepare(DownloadInfo downloadInfo) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onPrepare(downloadInfo);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onWaitingInQueue(DownloadInfo downloadInfo) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onWaitingInQueue(downloadInfo);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onConnected(DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onConnected(downloadInfo, responseHeaderFields);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onDownloading(String resKey, long totalSize, long currentSize, int progress, float speed) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onDownloading(resKey, totalSize, currentSize, progress, speed);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onRetrying(DownloadInfo downloadInfo, boolean deleteFile) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onRetrying(downloadInfo, deleteFile);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onPause(DownloadInfo downloadInfo) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onPause(downloadInfo);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onDelete(DownloadInfo downloadInfo) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onDelete(downloadInfo);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onSuccess(DownloadInfo downloadInfo) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onSuccess(downloadInfo);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void onFailure(DownloadInfo downloadInfo) {
        if (mTaskListenerMap.isEmpty()) return;
        Set<Map.Entry<Integer, ITaskListener>> entries = mTaskListenerMap.entrySet();
        Iterator<Map.Entry<Integer, ITaskListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ITaskListener> entry = iterator.next();
            ITaskListener listener = entry.getValue();
            try {
                listener.onFailure(downloadInfo);
            } catch (RemoteException e) {
                if (!isAlive(listener)) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean isAlive(ITaskListener listener) {
        try {
            return listener.isAlive();
        } catch (Exception e) {
            return false;
        }
    }
}
