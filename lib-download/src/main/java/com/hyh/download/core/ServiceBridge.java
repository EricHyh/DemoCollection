package com.hyh.download.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.hyh.download.Callback;
import com.hyh.download.IClient;
import com.hyh.download.IRequest;
import com.hyh.download.State;
import com.hyh.download.bean.TaskInfo;
import com.hyh.download.core.service.FDLService;
import com.hyh.download.db.TaskDatabaseHelper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/9.
 */

public class ServiceBridge implements IDownloadProxy {


    private final Object mServiceConnectedLock = new Object();

    private volatile boolean mIsConnected = false;
    private boolean mIsStartBind = false;
    private volatile ServiceConnection mConnection;
    private Context mContext;
    private TaskDatabaseHelper mDBUtil;
    private IRequest mServiceAgent;
    private Callback mCallback;
    private Map<String, TaskCache> mCacheTasks = new LinkedHashMap<>();

    private int mMaxSynchronousDownloadNum;
    private final int mPid;
    private Class mServiceClass;

    public ServiceBridge(Context context, boolean isIndependentProcess, int maxSynchronousDownloadNum, Callback callback) {
        this.mContext = context.getApplicationContext();
        this.mDBUtil = TaskDatabaseHelper.getInstance().init(mContext);
        this.mServiceClass = isIndependentProcess ? FDLService.IndependentProcessService.class : FDLService.MainProcessService.class;
        this.mMaxSynchronousDownloadNum = maxSynchronousDownloadNum;
        this.mCallback = callback;
        this.mPid = Process.myPid();
    }

    @Override
    public void initProxy() {
        waitingForService();
        if (mIsConnected) {
            try {
                mServiceAgent.fixDatabaseErrorStatus();
            } catch (RemoteException e) {
                mDBUtil.fixDatabaseErrorStatus();
            }
        } else {
            mDBUtil.fixDatabaseErrorStatus();
        }
    }

    private synchronized void bindService() {
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


    private IClient.Stub mClient = new IClient.Stub() {

        @Override
        public boolean isAlive() throws RemoteException {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onCallback(TaskInfo taskInfo) throws RemoteException {
            if (taskInfo == null) {
                return;
            }
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
            }
        }

        @Override
        public void onHaveNoTask() throws RemoteException {
            if (mCallback != null) {
                mCallback.onHaveNoTask();
            }
        }
    };

    private ServiceConnection getConnection() {
        return mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName name, IBinder service) {
                mServiceAgent = IRequest.Stub.asInterface(service);
                try {
                    mServiceAgent.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            onServiceDisconnected(name);
                        }
                    }, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                mIsConnected = true;
                mIsStartBind = false;
                try {
                    mServiceAgent.register(mPid, mClient);
                    synchronized (mServiceConnectedLock) {
                        mServiceConnectedLock.notifyAll();
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
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mConnection = null;
                mIsConnected = false;
                mIsStartBind = false;
                bindService();
            }
        };
    }

    @Override
    public boolean isTaskEnqueue(String resKey) {
        waitingForService();
        if (mIsConnected) {
            try {
                return mServiceAgent.isTaskEnqueue(resKey);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }

    @Override
    public void onReceiveStartCommand(TaskInfo taskInfo) {
        waitingForService();
        try {
            mServiceAgent.onReceiveStartCommand(taskInfo);
        } catch (Exception e) {

        }
    }

    @Override
    public void onReceivePauseCommand(TaskInfo taskInfo) {
        waitingForService();
        try {
            mServiceAgent.onReceivePauseCommand(taskInfo);
        } catch (Exception e) {

        }
    }

    @Override
    public void onReceiveDeleteCommand(TaskInfo taskInfo) {
        waitingForService();
        try {
            mServiceAgent.onReceiveDeleteCommand(taskInfo);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean isFileDownloading(String resKey) {
        return false;
    }

    @Override
    public boolean isFileDownloaded(String resKey) {
        return false;
    }

    @Override
    public TaskInfo getTaskInfoByKey(String resKey) {
        return null;
    }

    @Override
    public void deleteTask(String resKey) {

    }

    private void waitingForService() {
        if (!mIsConnected) {
            bindService();
            synchronized (mServiceConnectedLock) {
                while (true) {
                    try {
                        mServiceConnectedLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mIsConnected) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void operateDatabase(TaskInfo taskInfo) {
        waitingForService();
        try {
            mServiceAgent.operateDatabase(mPid, taskInfo);
        } catch (RemoteException e) {
            mDBUtil.operate(taskInfo);
        }
    }


    @Override
    public void enqueue(int command, TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        TaskCache taskCache = new TaskCache(command, taskInfo);
        mCacheTasks.put(resKey, taskCache);
        if (mIsConnected) {
            try {
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
}
