package com.hyh.download.core;

import android.os.Handler;
import android.os.Looper;

import com.hyh.download.Callback;
import com.hyh.download.CallbackAdapter;
import com.hyh.download.bean.TaskInfo;

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

    public TaskListenerManager() {
    }

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
    public void onPrepare(final TaskInfo taskInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = taskInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onPrepare(taskInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onNoEnoughSpace(final TaskInfo taskInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = taskInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onNoEnoughSpace(taskInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onFirstFileWrite(final TaskInfo taskInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = taskInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onFirstFileWrite(taskInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onDownloading(final TaskInfo taskInfo) {
        postBackThread(new Runnable() {
            @Override
            public void run() {
                String resKey = taskInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onDownloading(taskInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onWaitingInQueue(final TaskInfo taskInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = taskInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onWaitingInQueue(taskInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onWaitingForWifi(final TaskInfo taskInfo) {
        postUiThread(new Runnable() {
            @Override
            public void run() {
                String resKey = taskInfo.getResKey();
                List<Callback> singleCallbacks = getSingleCallbacks(resKey);
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onWaitingForWifi(taskInfo);
                    }
                }
            }
        });
    }

    @Override
    public void onDelete(final TaskInfo taskInfo) {
        final String resKey = taskInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onDelete(taskInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onPause(final TaskInfo taskInfo) {
        final String resKey = taskInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onPause(taskInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onSuccess(final TaskInfo taskInfo) {
        final String resKey = taskInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onSuccess(taskInfo);
                    }
                }
            }
        });
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
    }

    @Override
    public void onFailure(final TaskInfo taskInfo) {
        final String resKey = taskInfo.getResKey();
        final List<Callback> singleCallbacks = getSingleCallbacks(resKey);
        postUiThread(new Runnable() {
            @Override
            public void run() {
                if (singleCallbacks != null) {
                    for (Callback callback : singleCallbacks) {
                        callback.onFailure(taskInfo);
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
