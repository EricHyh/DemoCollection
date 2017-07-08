package com.eric.hyh.tools.download.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.eric.hyh.tools.download.IClient;
import com.eric.hyh.tools.download.IRequest;
import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.FileDownloader;
import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2017/3/9.
 */

public class ServiceBridge {


    private static final int MAX_CONNECTION_RETRY_TIMES = 3;
    private int mConnectionRetryTimes = 0;
    private volatile boolean mIsConnected = false;
    private boolean mIsStartBind = false;
    private volatile ServiceConnection mConnection;
    private Context mContext;
    private IRequest mServiceAgent;
    private Callback mCallback;
    private Map<String, Callback> mCallbacks = new HashMap<>();
    private Map<String, Type> mTypes = new HashMap<>();
    private Map<String, TaskCache> mCacheTasks = new LinkedHashMap<>();
    private Map<String, TaskInfo> mCacheDBTasks = new LinkedHashMap<>();
    private Map<String, Object> mTags = new HashMap<>();
    private HashMap<String, ArrayList<Callback>> mCallbackMap;
    private boolean mHasOtherProcess = true;
    private Executor mExecutor;
    private final int mPid;

    public ServiceBridge(Context context, Executor executor) {
        this.mContext = context.getApplicationContext();
        this.mExecutor = executor;
        this.mPid = Process.myPid();
    }

    public void bindService() {
        if (mIsConnected || mIsStartBind) {
            return;
        }
        mIsStartBind = true;
        if (mConnection == null) {
            mConnection = getConnection();
        }
        Intent intent = new Intent(mContext, FDLService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mContext.startService(intent);
    }

    public void unBindService() {
        if (mConnection != null) {
            try {
                int pid = Process.myPid();
                mServiceAgent.unRegister(pid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mContext.unbindService(mConnection);
            mConnection = null;
            mIsConnected = false;
        }
    }

    public void addCallback(String key, Callback callback) {
        ArrayList<Callback> callbackList;
        if (mCallbackMap == null) {
            mCallbackMap = new HashMap<>();
            callbackList = new ArrayList<>();
            callbackList.add(callback);
        } else {
            callbackList = mCallbackMap.get(key);
            if (callbackList == null) {
                callbackList = new ArrayList<>();
            }
            callbackList.add(callback);
        }
        mCallbackMap.put(key, callbackList);
    }

    public void putCallback(String resKey, Callback callback) {
        mCallbacks.put(resKey, callback);
    }

    public <T> void request(int command, TaskInfo taskInfo, Callback<T> callback) {
        String resKey = taskInfo.getResKey();
        TaskCache taskCache = new TaskCache(command, taskInfo, callback);
        mCacheTasks.put(resKey, taskCache);
        if (mIsConnected) {
            try {
                mTypes.put(resKey, taskInfo.getTagType());
                mTags.put(resKey, taskInfo.getTag());
                mCallbacks.put(resKey, callback);
                int pid = Process.myPid();
                mServiceAgent.register(pid, mClient);
                mServiceAgent.request(pid, command, taskInfo);
                mCacheTasks.remove(resKey);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindService();
        }
    }


    public void requestOperateDB(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mCacheDBTasks.put(resKey, taskInfo);
        if (mIsConnected) {
            TaskInfo remove = mCacheDBTasks.remove(resKey);
            try {
                mServiceAgent.onCall(mPid, remove);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindService();
        }
    }


    public boolean correctDBErroStatus() {
        if (mIsConnected) {
            try {
                mServiceAgent.correctDBErroStatus();
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindService();
        }
        return false;
    }

    public <T> void setCallback(Callback<T> callBack) {
        this.mCallback = callBack;
    }


    private IClient.Stub mClient = new IClient.Stub() {
        @SuppressWarnings("unchecked")
        @Override
        public void onCall(TaskInfo taskInfo) throws RemoteException {
            if (taskInfo == null) {
                return;
            }
            String resKey = taskInfo.getResKey();
            ArrayList<Callback> singleCallbacks = getSingleCallbacks(resKey);
            Callback singleCallback = mCallbacks.get(resKey);
            supplementTaskInfo(taskInfo);
            switch (taskInfo.getCurrentStatus()) {
                case State.PREPARE:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onPrepare(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onPrepare(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onPrepare(taskInfo);
                    }
                    break;
                case State.START_WRITE:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onFirstFileWrite(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onFirstFileWrite(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onFirstFileWrite(taskInfo);
                    }
                    break;
                case State.DOWNLOADING:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onDownloading(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onDownloading(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onDownloading(taskInfo);
                    }
                    break;
                case State.WAITING_IN_QUEUE:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onWaitingInQueue(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onWaitingInQueue(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onWaitingInQueue(taskInfo);
                    }
                    break;
                case State.WAITING_FOR_WIFI:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onWaitingForWifi(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onWaitingForWifi(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onWaitingForWifi(taskInfo);
                    }
                    break;
                case State.PAUSE:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onPause(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onPause(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onPause(taskInfo);
                    }
                    mCallbacks.remove(resKey);
                    if (mCallbackMap != null) {
                        mCallbackMap.remove(resKey);
                    }
                    break;
                case State.DELETE:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onDelete(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onDelete(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onDelete(taskInfo);
                    }
                    mCallbacks.remove(resKey);
                    if (mCallbackMap != null) {
                        mCallbackMap.remove(resKey);
                    }
                    break;
                case State.SUCCESS:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onSuccess(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onSuccess(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onSuccess(taskInfo);
                    }
                    if (TextUtils.isEmpty(taskInfo.getPackageName())) {
                        mCallbacks.remove(resKey);
                    }
                    if (mCallbackMap != null && TextUtils.isEmpty(taskInfo.getPackageName())) {
                        mCallbackMap.remove(resKey);
                    }
                    break;
                case State.FAILURE:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onFailure(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onFailure(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onFailure(taskInfo);
                    }
                    mCallbacks.remove(resKey);
                    if (mCallbackMap != null) {
                        mCallbackMap.remove(resKey);
                    }
                    break;
                case State.INSTALL:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onInstall(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onInstall(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onInstall(taskInfo);
                    }
                    break;
                case State.UNINSTALL:
                    if (singleCallbacks != null) {
                        for (Callback callback : singleCallbacks) {
                            callback.onUnInstall(taskInfo);
                        }
                    }
                    if (singleCallback != null) {
                        singleCallback.onUnInstall(taskInfo);
                    }
                    if (mCallback != null) {
                        mCallback.onUnInstall(taskInfo);
                    }
                    mCallbacks.remove(resKey);
                    if (mCallbackMap != null) {
                        mCallbackMap.remove(resKey);
                    }
                    break;
            }
        }

        @Override
        public void otherProcessCommand(int command, String resKey) throws RemoteException {
            switch (command) {
                case Command.PAUSE:
                    FileDownloader.getInstance(mContext).pauseTask(resKey);
                    break;
                case Command.DELETE:
                    FileDownloader.getInstance(mContext).deleteTask(resKey);
                    break;
            }
        }


        @Override
        public void onHaveNoTask() throws RemoteException {
            if (mCallback != null) {
                mCallback.onHaveNoTask();
            }
        }

        @Override
        public boolean isFileDownloading(String resKey) throws RemoteException {
            return FileDownloader.getInstance(mContext).isFileDownloading(resKey);
        }

        @Override
        public void onProcessChanged(boolean hasOtherProcess) throws RemoteException {
            mHasOtherProcess = hasOtherProcess;
        }
    };

    @SuppressWarnings("unchecked")
    private void supplementTaskInfo(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        Type type = mTypes.get(resKey);
        Object tag = mTags.get(resKey);
        taskInfo.setTagType(type);
        taskInfo.setTag(tag);
    }


    private ServiceConnection getConnection() {
        return mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mConnectionRetryTimes = 0;
                mServiceAgent = IRequest.Stub.asInterface(service);
                mIsConnected = true;
                mIsStartBind = false;
                try {
                    mServiceAgent.register(mPid, mClient);
                    synchronized (ServiceBridge.class) {
                        ServiceBridge.class.notifyAll();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (mCacheTasks.size() > 0) {
                    Collection<TaskCache> values = mCacheTasks.values();
                    for (TaskCache taskCache : values) {
                        request(taskCache.command, taskCache.taskInfo, taskCache.callback);
                    }
                }
                if (mCacheDBTasks.size() > 0) {
                    Collection<TaskInfo> values = mCacheDBTasks.values();
                    for (TaskInfo taskInfo : values) {
                        requestOperateDB(taskInfo);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mIsStartBind = false;
                if (mConnectionRetryTimes++ >= MAX_CONNECTION_RETRY_TIMES) {
                    bindService();
                }
            }
        };
    }

    private ArrayList<Callback> getSingleCallbacks(String key) {
        ArrayList<Callback> callbacks = null;
        if (mCallbackMap != null) {
            callbacks = mCallbackMap.get(key);
        }
        return callbacks;
    }

    public boolean isFileDownloading(String resKey) {
        if (mHasOtherProcess) {
            try {
                if (mIsConnected) {
                    return mServiceAgent.isFileDownloading(mPid, resKey);
                } else {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            bindService();
                        }
                    });
                    synchronized (ServiceBridge.class) {
                        while (true) {
                            ServiceBridge.class.wait();
                            if (mIsConnected) {
                                break;
                            }
                        }
                        return mServiceAgent.isFileDownloading(mPid, resKey);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void onUnInstall(TaskInfo taskInfo) {
        try {
            mClient.onCall(taskInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onInstall(TaskInfo taskInfo) {
        try {
            mClient.onCall(taskInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
