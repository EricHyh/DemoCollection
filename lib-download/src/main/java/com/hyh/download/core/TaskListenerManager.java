package com.hyh.download.core;

import com.hyh.download.DownloadInfo;
import com.hyh.download.TaskListener;
import com.hyh.download.utils.L;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2017/7/11
 */
@SuppressWarnings("unchecked")
public class TaskListenerManager implements TaskListener {


    private final ConcurrentHashMap<String, List<TaskListener>> mListenersMap = new ConcurrentHashMap<>();

    public void addSingleTaskListener(String key, TaskListener listener) {
        List<TaskListener> callbackList;
        callbackList = mListenersMap.get(key);
        if (callbackList == null) {
            callbackList = new CopyOnWriteArrayList<>();
            callbackList.add(listener);
            mListenersMap.put(key, callbackList);
        } else {
            if (!callbackList.contains(listener)) {
                callbackList.add(listener);
            }
        }
    }

    public void removeSingleTaskListener(String resKey, TaskListener listener) {
        List<TaskListener> singleCallbacks = getSingleListeners(resKey);
        if (singleCallbacks != null) {
            singleCallbacks.remove(listener);
        }
    }

    public void removeSingleTaskCallbacks(String resKey) {
        mListenersMap.remove(resKey);
    }

    private List<TaskListener> getSingleListeners(String key) {
        return mListenersMap.get(key);
    }

    @Override
    public void onPrepare(final DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onPrepare(downloadInfo);
            }
        }
    }

    @Override
    public void onWaitingStart(final DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onWaitingStart(downloadInfo);
            }
        }
    }

    @Override
    public void onWaitingEnd(DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onWaitingEnd(downloadInfo);
            }
        }
    }

    @Override
    public void onConnected(DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onConnected(downloadInfo, responseHeaderFields);
            }
        }
    }

    @Override
    public void onDownloading(String resKey, long totalSize, long currentSize, int progress, float speed) {
        L.d("onDownloading: " + resKey + ", progress = " + progress);
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onDownloading(resKey, totalSize, currentSize, progress, speed);
            }
        }
    }

    @Override
    public void onRetrying(DownloadInfo downloadInfo, boolean deleteFile) {
        List<TaskListener> singleListeners = getSingleListeners(downloadInfo.getResKey());
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onRetrying(downloadInfo, deleteFile);
            }
        }
    }


    @Override
    public void onPause(final DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onPause(downloadInfo);
            }
        }
    }

    @Override
    public void onDelete(final DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onDelete(downloadInfo);
            }
        }
    }

    @Override
    public void onSuccess(final DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        L.d("onSuccess: " + resKey);
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onSuccess(downloadInfo);
            }
        }
    }

    @Override
    public void onFailure(DownloadInfo downloadInfo) {
        String resKey = downloadInfo.getResKey();
        List<TaskListener> singleListeners = getSingleListeners(resKey);
        if (singleListeners != null) {
            for (TaskListener listener : singleListeners) {
                listener.onFailure(downloadInfo);
            }
        }
    }
}
