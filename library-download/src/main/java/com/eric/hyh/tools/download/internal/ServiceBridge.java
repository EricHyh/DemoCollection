package com.eric.hyh.tools.download.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.eric.hyh.tools.download.IClient;
import com.eric.hyh.tools.download.IRequest;
import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.FileDownloader;
import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2017/3/9.
 */

public class ServiceBridge implements IDownloadProxy.ILocalDownloadProxy {


    private static final int MAX_CONNECTION_RETRY_TIMES = 3;

    private int mConnectionRetryTimes = 0;
    private volatile boolean mIsConnected = false;
    private boolean mIsStartBind = false;
    private volatile ServiceConnection mConnection;
    private Context mContext;
    private IRequest mServiceAgent;
    private Callback mCallback;
    private Map<String, Type> mTypes = new HashMap<>();
    private Map<String, TaskCache> mCacheTasks = new LinkedHashMap<>();
    private Map<String, TaskInfo> mCacheDBTasks = new LinkedHashMap<>();
    private Map<String, Object> mTags = new HashMap<>();

    private boolean mHasOtherProcess = true;
    private Executor mExecutor;
    private int mMaxSynchronousDownloadNum;
    private final int mPid;

    public ServiceBridge(Context context, Executor executor, int maxSynchronousDownloadNum) {
        this.mContext = context.getApplicationContext();
        this.mExecutor = executor;
        this.mMaxSynchronousDownloadNum = maxSynchronousDownloadNum;
        this.mPid = Process.myPid();
    }

    @Override
    public void initProxy(final FileDownloader.LockConfig lockConfig) {
        if (!Utils.isServiceRunning(mContext, FDLService.class.getName())) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Utils.DBUtil.getInstance(mContext).correctDBErroStatus(mContext);
                    synchronized (lockConfig) {
                        lockConfig.setInitProxyFinish(true);
                        lockConfig.notifyAll();
                    }
                }
            });
        } else {
            synchronized (lockConfig) {
                lockConfig.setInitProxyFinish(true);
                lockConfig.notifyAll();
            }
        }
    }


    public synchronized void bindService() {
        if (mIsConnected || mIsStartBind) {
            return;
        }
        mIsStartBind = true;
        if (mConnection == null) {
            mConnection = getConnection();
        }
        Intent intent = new Intent(mContext, FDLService.class);
        intent.putExtra(Constants.MAX_SYNCHRONOUS_DOWNLOAD_NUM, mMaxSynchronousDownloadNum);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mContext.startService(intent);
    }

    public synchronized void unBindService() {
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

    private IClient.Stub mClient = new IClient.Stub() {
        @SuppressWarnings("unchecked")
        @Override
        public void onCall(TaskInfo taskInfo) throws RemoteException {
            if (taskInfo == null) {
                return;
            }
            supplementTaskInfo(taskInfo);
            switch (taskInfo.getCurrentStatus()) {
                case State.PREPARE:
                    if (mCallback != null) {
                        mCallback.onPrepare(taskInfo);
                    }
                    break;
                case State.START_WRITE:
                    if (mCallback != null) {
                        mCallback.onFirstFileWrite(taskInfo);
                    }
                    break;
                case State.DOWNLOADING:
                    if (mCallback != null) {
                        mCallback.onDownloading(taskInfo);
                    }
                    break;
                case State.WAITING_IN_QUEUE:
                    if (mCallback != null) {
                        mCallback.onWaitingInQueue(taskInfo);
                    }
                    break;
                case State.WAITING_FOR_WIFI:
                    if (mCallback != null) {
                        mCallback.onWaitingForWifi(taskInfo);
                    }
                    break;
                case State.PAUSE:
                    if (mCallback != null) {
                        mCallback.onPause(taskInfo);
                    }
                    break;
                case State.DELETE:
                    if (mCallback != null) {
                        mCallback.onDelete(taskInfo);
                    }
                    break;
                case State.SUCCESS:
                    if (mCallback != null) {
                        mCallback.onSuccess(taskInfo);
                    }
                    break;
                case State.FAILURE:
                    if (mCallback != null) {
                        mCallback.onFailure(taskInfo);
                    }
                    break;
                case State.INSTALL:
                    if (mCallback != null) {
                        mCallback.onInstall(taskInfo);
                    }
                    break;
                case State.UNINSTALL:
                    if (mCallback != null) {
                        mCallback.onUnInstall(taskInfo);
                    }
                    break;
            }
        }

        @Override
        public void otherProcessCommand(int command, String resKey) throws RemoteException {
            switch (command) {
                case Command.PAUSE:
                    FileDownloader.getInstance().pauseTask(resKey);
                    break;
                case Command.DELETE:
                    FileDownloader.getInstance().deleteTask(resKey);
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
            return FileDownloader.getInstance().isFileDownloading(resKey);
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
                        enqueue(taskCache.command, taskCache.taskInfo);
                    }
                }
                if (mCacheDBTasks.size() > 0) {
                    Collection<TaskInfo> values = mCacheDBTasks.values();
                    for (TaskInfo taskInfo : values) {
                        operateDatebase(taskInfo);
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

    @Override
    public void setAllTaskCallback(Callback callBack) {
        this.mCallback = callBack;
    }

    @Override
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

    @Override
    public void operateDatebase(TaskInfo taskInfo) {
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


    @Override
    public void enqueue(int command, TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        TaskCache taskCache = new TaskCache(command, taskInfo);
        mCacheTasks.put(resKey, taskCache);
        if (mIsConnected) {
            try {
                mTypes.put(resKey, taskInfo.getTagType());
                mTags.put(resKey, taskInfo.getTag());
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

    @Override
    public void setMaxSynchronousDownloadNum(int num) {
        mMaxSynchronousDownloadNum = num;
    }

    @Override
    public void destroy() {
        unBindService();
    }
}
