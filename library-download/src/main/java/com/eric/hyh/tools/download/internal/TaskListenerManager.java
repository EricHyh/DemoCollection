package com.eric.hyh.tools.download.internal;

import android.text.TextUtils;
import android.util.Log;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.CallbackAdapter;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 * @description
 * @data 2017/7/11
 */
@SuppressWarnings("unchecked")
public class TaskListenerManager extends CallbackAdapter {


    private Callback mAllTaskListener;
    private ConcurrentHashMap<String, ArrayList<Callback>> mCallbacksMap;

    public TaskListenerManager(Callback allTaskListener) {
        this.mAllTaskListener = allTaskListener;
    }

    public void addSingleTaskCallback(String key, Callback callback) {
        ArrayList<Callback> callbackList;
        if (mCallbacksMap == null) {
            mCallbacksMap = new ConcurrentHashMap<>();
            callbackList = new ArrayList<>();
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

    private ArrayList<Callback> getSingleCallbacks(String key) {
        ArrayList<Callback> callbacks = null;
        if (mCallbacksMap != null) {
            callbacks = mCallbacksMap.get(key);
        }
        return callbacks;
    }

    @Override
    public void onNoEnoughSpace(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onNoEnoughSpace(taskInfo);
            }
        }
        mAllTaskListener.onNoEnoughSpace(taskInfo);
    }

    @Override
    public void onPrepare(TaskInfo taskInfo) {
        Log.d("FDL_HH", "onPrepare notify " + taskInfo.getResKey());
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onPrepare(taskInfo);
            }
        }
        mAllTaskListener.onPrepare(taskInfo);
    }

    @Override
    public void onFirstFileWrite(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onNoEnoughSpace(taskInfo);
            }
        }
        mAllTaskListener.onFirstFileWrite(taskInfo);
    }

    @Override
    public void onDownloading(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onDownloading(taskInfo);
            }
        }
        mAllTaskListener.onDownloading(taskInfo);
    }

    @Override
    public void onWaitingInQueue(TaskInfo taskInfo) {
        Log.d("FDL_HH", "onWaitingInQueue notify " + taskInfo.getResKey());
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onWaitingInQueue(taskInfo);
            }
        }
        mAllTaskListener.onWaitingInQueue(taskInfo);
    }

    @Override
    public void onWaitingForWifi(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onWaitingForWifi(taskInfo);
            }
        }
        mAllTaskListener.onWaitingForWifi(taskInfo);
    }

    @Override
    public void onDelete(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onDelete(taskInfo);
            }
        }
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
        mAllTaskListener.onDelete(taskInfo);
    }

    @Override
    public void onPause(TaskInfo taskInfo) {
        Log.d("FDL_HH", "onWaitingInQueue onPause " + taskInfo.getResKey());
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onPause(taskInfo);
            }
        }
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
        mAllTaskListener.onPause(taskInfo);
    }

    @Override
    public void onSuccess(TaskInfo taskInfo) {
        Log.d("FDL_HH", "onWaitingInQueue onSuccess " + taskInfo.getResKey());
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onSuccess(taskInfo);
            }
        }
        if (mCallbacksMap != null && TextUtils.isEmpty(taskInfo.getPackageName())) {
            mCallbacksMap.remove(resKey);
        }
        mAllTaskListener.onSuccess(taskInfo);
    }

    @Override
    public void onInstall(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onInstall(taskInfo);
            }
        }
        mAllTaskListener.onInstall(taskInfo);
    }

    @Override
    public void onUnInstall(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onUnInstall(taskInfo);
            }
        }
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
        mAllTaskListener.onUnInstall(taskInfo);
    }

    @Override
    public void onFailure(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
        if (singleCallbacks != null) {
            for (Callback callback : singleCallbacks) {
                callback.onFailure(taskInfo);
            }
        }
        if (mCallbacksMap != null) {
            mCallbacksMap.remove(resKey);
        }
        mAllTaskListener.onFailure(taskInfo);
    }

    @Override
    public void onHaveNoTask() {
        mAllTaskListener.onHaveNoTask();
    }
}
