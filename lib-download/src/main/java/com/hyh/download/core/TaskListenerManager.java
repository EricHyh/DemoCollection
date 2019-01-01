package com.hyh.download.core;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.hyh.download.Callback;
import com.hyh.download.DownloadInfo;
import com.hyh.download.utils.L;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2017/7/11
 */
@SuppressWarnings("unchecked")
public class TaskListenerManager implements Callback {

    private final Map<String, Speed> mSpeedMap = new ConcurrentHashMap<>();

    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    private boolean mIsPostUiThread = true;

    private final ThreadPoolExecutor mBackExecutor;

    {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 120L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "TaskListenerManager");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();
        mBackExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, workQueue, threadFactory, handler);
        mBackExecutor.allowCoreThreadTimeOut(true);
    }

    private final ConcurrentHashMap<String, List<Callback>> mCallbacksMap = new ConcurrentHashMap<>();

    public void addSingleTaskCallback(String key, Callback callback) {
        List<Callback> callbackList;
        callbackList = mCallbacksMap.get(key);
        if (callbackList == null) {
            callbackList = new CopyOnWriteArrayList<>();
            callbackList.add(callback);
            mCallbacksMap.put(key, callbackList);
        } else {
            if (!callbackList.contains(callback)) {
                callbackList.add(callback);
            }
        }
    }

    public void removeSingleTaskCallback(String resKey, Callback callback) {
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            singleCallbacks.remove(callback);
        }
    }

    public void removeSingleTaskCallbacks(String resKey) {
        mCallbacksMap.remove(resKey);
    }

    private List<Callback> getSingleCallbacks(String key) {
        return mCallbacksMap.get(key);
    }

    @Override
    public void onPrepare(final DownloadInfo downloadInfo) {
        L.d("onPrepare:" + downloadInfo.getProgress());
        String resKey = downloadInfo.getResKey();
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onPrepare(downloadInfo);
            }
        }
    }

    @Override
    public void onWaitingInQueue(final DownloadInfo downloadInfo) {
        L.d("onWaitingInQueue");
        String resKey = downloadInfo.getResKey();
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onWaitingInQueue(downloadInfo);
            }
        }
    }

    @Override
    public void onDownloading(final DownloadInfo downloadInfo) {
        L.d("onDownloading:" + downloadInfo.getProgress());
        final String resKey = downloadInfo.getResKey();
        Speed speed = mSpeedMap.get(resKey);
        if (speed == null) {
            speed = new Speed();
            mSpeedMap.put(resKey, speed);
        }
        //downloadInfo.setSpeed(speed.computeSpeed(downloadInfo.getCurrentSize()));
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onDownloading(downloadInfo);
            }
        }
    }

    @Override
    public void onPause(final DownloadInfo downloadInfo) {
        L.d("onPause");
        final String resKey = downloadInfo.getResKey();
        mSpeedMap.remove(resKey);
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onPause(downloadInfo);
            }
        }
    }

    @Override
    public void onDelete(final DownloadInfo downloadInfo) {
        L.d("onDelete");
        final String resKey = downloadInfo.getResKey();
        mSpeedMap.remove(resKey);
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onDelete(downloadInfo);
            }
        }
    }

    @Override
    public void onSuccess(final DownloadInfo downloadInfo) {
        L.d("onSuccess");
        final String resKey = downloadInfo.getResKey();
        mSpeedMap.remove(resKey);
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onSuccess(downloadInfo);
            }
        }
    }

    @Override
    public void onWaitingForWifi(final DownloadInfo downloadInfo) {
        L.d("onWaitingForWifi");
        final String resKey = downloadInfo.getResKey();
        mSpeedMap.remove(resKey);
        List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onWaitingForWifi(downloadInfo);
            }
        }
    }

    @Override
    public void onLowDiskSpace(final DownloadInfo downloadInfo) {
        L.d("onLowDiskSpace");
        final String resKey = downloadInfo.getResKey();
        mSpeedMap.remove(resKey);
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onLowDiskSpace(downloadInfo);
            }
        }
    }

    @Override
    public void onFailure(final DownloadInfo downloadInfo) {
        L.d("onFailure");
        final String resKey = downloadInfo.getResKey();
        mSpeedMap.remove(resKey);
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onFailure(downloadInfo);
            }
        }
    }

    private static class Speed {

        private long lastFileSize;

        private long lastTimeMills;

        private float speed = 0.0f;

        float computeSpeed(long currentSize) {
            long elapsedTimeMillis = SystemClock.elapsedRealtime();
            if (lastTimeMills == 0) {
                lastFileSize = currentSize;
                lastTimeMills = elapsedTimeMillis;
                return 0.0f;
            } else {
                long diffTimeMillis = elapsedTimeMillis - lastTimeMills;
                if (speed == 0 || diffTimeMillis >= 2000) {
                    long diffSize = currentSize - lastFileSize;
                    lastFileSize = currentSize;
                    lastTimeMills = elapsedTimeMillis;
                    if (diffSize <= 0 || diffTimeMillis <= 0) {
                        return 0.0f;
                    }
                    speed = (diffSize * 1000.0f) / (diffTimeMillis * 1024.0f);
                }
                return speed;
            }
        }
    }
}
