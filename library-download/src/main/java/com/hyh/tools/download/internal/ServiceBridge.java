package com.hyh.tools.download.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.hyh.tools.download.IClient;
import com.hyh.tools.download.IRequest;
import com.hyh.tools.download.api.Callback;
import com.hyh.tools.download.api.FileDownloader;
import com.hyh.tools.download.bean.Command;
import com.hyh.tools.download.bean.Constants;
import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TagInfo;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.utils.FD_DBUtil;
import com.hyh.tools.download.utils.FD_PackageUtil;

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
    private Map<String, TaskCache> mCacheTasks = new LinkedHashMap<>();
    private Map<String, TaskInfo> mCacheDBTasks = new LinkedHashMap<>();
    private Map<String, Object> mTags = new HashMap<>();

    private boolean mHasOtherProcess = false;
    private Executor mExecutor;
    private int mMaxSynchronousDownloadNum;
    private final int mPid;
    private Class mServiceClass;

    public ServiceBridge(Context context, boolean isIndependentProcess, Executor executor, int maxSynchronousDownloadNum) {
        this.mContext = context.getApplicationContext();
        this.mServiceClass = isIndependentProcess ? FDLService.IndependentProcessService.class : FDLService.MainProcessService.class;
        this.mExecutor = executor;
        this.mMaxSynchronousDownloadNum = maxSynchronousDownloadNum;
        this.mPid = Process.myPid();
    }

    @Override
    public void initProxy(final FileDownloader.LockConfig lockConfig) {
        if (!FD_PackageUtil.isServiceRunning(mContext, mServiceClass.getName())) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    FD_DBUtil.getInstance(mContext).reviseDateBaseErroStatus(mContext);
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
        Intent intent = new Intent(mContext, mServiceClass);
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

    private void supplementTaskInfo(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        Object tag = mTags.get(resKey);
        TagInfo tagInfo = taskInfo.getTagInfo();
        if (tagInfo != null) {
            tagInfo.setTag(tag);
        }
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
    public boolean isOtherProcessDownloading(String resKey) {
        if (mIsConnected) {
            if (mHasOtherProcess) {
                try {
                    return mServiceAgent.isFileDownloading(mPid, resKey);
                } catch (RemoteException e) {//这里很少报错，测试还没出现过
                    e.printStackTrace();
                }
            }
            return false;
        } else {
            //先连接服务
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            });
            synchronized (ServiceBridge.class) {
                while (true) {
                    try {
                        ServiceBridge.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mIsConnected) {
                        break;
                    }
                }
                return isOtherProcessDownloading(resKey);
            }
        }
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
                mTags.put(resKey, taskInfo.getTagInfo().getTag());
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
