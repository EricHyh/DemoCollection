package com.hyh.download.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.hyh.download.DownloadInfo;
import com.hyh.download.DownloaderConfig;
import com.hyh.download.IFileChecker;
import com.hyh.download.IRequest;
import com.hyh.download.ITaskListener;
import com.hyh.download.TaskListener;
import com.hyh.download.core.service.FDLService;
import com.hyh.download.db.bean.TaskInfo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/3/9.
 */

public class ServiceBridge implements IDownloadProxy {


    private final ThreadPoolExecutor mExecutor;

    {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 120L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "FileDownloader");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    private final Object mServiceConnectedLock = new Object();

    private volatile boolean mIsConnected = false;
    private volatile boolean mIsStartBind = false;
    private volatile ServiceConnection mConnection;
    private Context mContext;
    private IRequest mServiceAgent;
    private TaskListener mTaskListener;
    private Map<String, TaskCache> mCacheTasks = new LinkedHashMap<>();

    private DownloaderConfig mDownloaderConfig;
    private final int mPid;
    private Class mServiceClass;

    public ServiceBridge(Context context, DownloaderConfig downloaderConfig, TaskListener taskListener) {
        this.mContext = context.getApplicationContext();
        this.mServiceClass = downloaderConfig.isIndependentProcess() ?
                FDLService.IndependentProcessService.class : FDLService.MainProcessService.class;
        this.mDownloaderConfig = downloaderConfig;
        this.mTaskListener = taskListener;
        this.mPid = Process.myPid();
    }

    @Override
    public void initProxy(Runnable afterInit) {
        waitingForService();
        try {
            mServiceAgent.initDownloadProxy(
                    DownloadProxyConfig.create(mDownloaderConfig),
                    mDownloaderConfig.getGlobalFileChecker());
            afterInit.run();
        } catch (Exception e) {
            //
        }
    }

    private synchronized void bindService() {
        if (isServiceAlive()) {
            return;
        }
        if (mIsStartBind) {
            return;
        }
        mIsStartBind = true;
        if (mConnection == null) {
            mConnection = getConnection();
        }
        Intent intent = new Intent(mContext, mServiceClass);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mContext.startService(intent);
    }

    @Override
    public synchronized void startTask(TaskInfo taskInfo, IFileChecker fileChecker) {
        waitingForService();
        try {
            String resKey = taskInfo.getResKey();

            mCacheTasks.put(resKey, new TaskCache(Command.START, taskInfo, fileChecker));
            mServiceAgent.startTask(taskInfo, fileChecker);
            mCacheTasks.remove(resKey);

        } catch (Exception e) {
            //
        }
    }

    @Override
    public synchronized void pauseTask(String resKey) {
        waitingForService();
        try {

            mServiceAgent.pauseTask(resKey);
        } catch (Exception e) {
            //
        }
    }

    @Override
    public synchronized void deleteTask(String resKey) {
        waitingForService();

    }

    @Override
    public boolean isTaskAlive(String resKey) {
        waitingForService();
        try {
            return mServiceAgent.isTaskAlive(resKey);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isFileDownloaded(String resKey, IFileChecker fileChecker) {
        waitingForService();
        try {
            return mServiceAgent.isFileDownloaded(resKey, fileChecker);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TaskInfo getTaskInfoByKey(String resKey) {
        waitingForService();
        try {
            return mServiceAgent.getTaskInfoByKey(resKey);
        } catch (Exception e) {
            return null;
        }
    }

    private void waitingForService() {
        if (isServiceAlive()) {
            return;
        }
        bindService();
        synchronized (mServiceConnectedLock) {
            while (true) {
                try {
                    mServiceConnectedLock.wait();
                } catch (Exception e) {
                    //
                }
                if (isServiceAlive()) {
                    break;
                }
            }
        }
    }

    private boolean isServiceAlive() {
        if (mIsConnected && mServiceAgent != null) {
            try {
                return mServiceAgent.isAlive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void insertOrUpdate(TaskInfo taskInfo) {
        waitingForService();
        try {
            mServiceAgent.insertOrUpdate(taskInfo);
        } catch (RemoteException e) {
            //
        }
    }


    private ITaskListener.Stub mServiceTaskListener = new ITaskListener.Stub() {
        @Override
        public boolean isAlive() throws RemoteException {
            return true;
        }

        @Override
        public void onPrepare(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onPrepare(downloadInfo);
        }

        @Override
        public void onWaitingStart(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onWaitingStart(downloadInfo);
        }

        @Override
        public void onWaitingEnd(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onWaitingEnd(downloadInfo);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onConnected(DownloadInfo downloadInfo, Map responseHeaderFields) throws RemoteException {
            mTaskListener.onConnected(downloadInfo, responseHeaderFields);
        }

        @Override
        public void onDownloading(String resKey, long totalSize, long currentSize, int progress, float speed) throws RemoteException {
            mTaskListener.onDownloading(resKey, totalSize, currentSize, progress, speed);
        }

        @Override
        public void onRetrying(DownloadInfo downloadInfo, boolean deleteFile) throws RemoteException {
            mTaskListener.onRetrying(downloadInfo, deleteFile);
        }

        @Override
        public void onPause(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onPause(downloadInfo);
        }

        @Override
        public void onDelete(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onDelete(downloadInfo);
        }

        @Override
        public void onSuccess(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onSuccess(downloadInfo);
        }

        @Override
        public void onFailure(DownloadInfo downloadInfo) throws RemoteException {
            mTaskListener.onFailure(downloadInfo);
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
                    mServiceAgent.register(mPid, mServiceTaskListener);
                    synchronized (mServiceConnectedLock) {
                        mServiceConnectedLock.notifyAll();
                    }
                    if (mCacheTasks.size() > 0) {
                        Collection<TaskCache> values = mCacheTasks.values();
                        for (TaskCache taskCache : values) {
                            int command = taskCache.command;
                            switch (command) {
                                case Command.START: {
                                    startTask(taskCache.taskInfo, taskCache.fileChecker);
                                    break;
                                }
                                case Command.PAUSE: {
                                    pauseTask(taskCache.resKey);
                                    break;
                                }
                                case Command.DELETE: {
                                    deleteTask(taskCache.resKey);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    //
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
}
