package com.hyh.download.core;

import android.os.Handler;
import android.os.Looper;

import com.hyh.download.Callback;
import com.hyh.download.CallbackAdapter;
import com.hyh.download.DownloadInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
public class TaskListenerManager extends CallbackAdapter {

    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    private final ThreadPoolExecutor mBackExecutor;

    {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 120L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(2);
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

    private ConcurrentHashMap<String, List<Callback>> mCallbacksMap;

    public void addSingleTaskCallback(String key, Callback callback) {
        List<Callback> callbackList;
        if (mCallbacksMap == null) {
            mCallbacksMap = new ConcurrentHashMap<>();
            callbackList = new CopyOnWriteArrayList<>();
            callbackList.add(callback);
        } else {
            callbackList = mCallbacksMap.get(key);
            if (callbackList == null) {
                callbackList = new ArrayList<>();
            }
            callbackList.add(callback);
        }
        mCallbacksMap.put(key, callbackList);
    }

    private List<Callback> getSingleCallbacks(String key) {
        List<Callback> callbacks = null;
        if (mCallbacksMap != null) {
            callbacks = mCallbacksMap.get(key);
        }
        return callbacks;
    }

    @Override
    public void onPrepare(final DownloadInfo downloadInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = downloadInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onPrepare(downloadInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onWaitingInQueue(final DownloadInfo downloadInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = downloadInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onWaitingInQueue(downloadInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onDownloading(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        postUiThread(new Runnable() {
            @Override
            public void run() {
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onDownloading(downloadInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onPause(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onPause(downloadInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onDelete(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onDelete(downloadInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onSuccess(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onSuccess(downloadInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onWaitingForWifi(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onWaitingForWifi(downloadInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onLowDiskSpace(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onLowDiskSpace(downloadInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onFailure(final DownloadInfo downloadInfo) {
        final String resKey = downloadInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onFailure(downloadInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onHaveNoTask() {
        postUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void postUiThread(Runnable runnable) {
        mUiHandler.post(runnable);
    }

    private void postBackThread(Runnable runnable) {
        mBackExecutor.execute(runnable);
    }
}
