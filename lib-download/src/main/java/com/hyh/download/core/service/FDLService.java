package com.hyh.download.core.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.hyh.download.IFileChecker;
import com.hyh.download.IRequest;
import com.hyh.download.ITaskListener;
import com.hyh.download.TaskListener;
import com.hyh.download.core.DownloadProxyConfig;
import com.hyh.download.core.DownloadProxyImpl;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.ITaskListenerWrapper;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.utils.L;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/3/8.
 */

public class FDLService extends Service {

    private final Object mInitProxyLock = new Object();

    private final Map<Integer, ITaskListener> mListenerMap = new ConcurrentHashMap<>();

    private final TaskListener mTaskListener = new ITaskListenerWrapper(mListenerMap);

    private volatile IDownloadProxy mServiceProxy;

    private volatile boolean mIsInitProxy;

    private IRequest mAgent = new IRequest.Stub() {

        @Override
        public boolean isAlive() throws RemoteException {
            return true;
        }

        @Override
        public synchronized void initDownloadProxy(DownloadProxyConfig downloadProxyConfig, IFileChecker globalFileChecker) throws RemoteException {
            if (mServiceProxy != null) {
                mServiceProxy = new DownloadProxyImpl(
                        getApplicationContext(),
                        downloadProxyConfig,
                        mTaskListener,
                        globalFileChecker);
                initProxy();
            }
            waitingForInitProxy();
        }

        @Override
        public void register(int pid, ITaskListener listener) throws RemoteException {
            waitingForInitProxy();
            mListenerMap.put(pid, listener);
        }

        @Override
        public void insertOrUpdate(TaskInfo taskInfo) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.insertOrUpdate(taskInfo);
        }

        @Override
        public synchronized boolean isTaskAlive(String resKey) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.isTaskAlive(resKey);
        }

        @Override
        public synchronized boolean isFileDownloaded(String resKey, IFileChecker fileChecker) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.isFileDownloaded(resKey, fileChecker);
        }

        @Override
        public TaskInfo getTaskInfoByKey(String resKey) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.getTaskInfoByKey(resKey);
        }

        @Override
        public void startTask(TaskInfo taskInfo, IFileChecker fileChecker) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.startTask(taskInfo, fileChecker);
        }

        @Override
        public void pauseTask(String resKey) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.pauseTask(resKey);
        }

        @Override
        public void deleteTask(String resKey) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.deleteTask(resKey);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        L.d("bind service");
        return mAgent.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initProxy() {
        mServiceProxy.initProxy(new Runnable() {
            @Override
            public void run() {
                synchronized (mInitProxyLock) {
                    mIsInitProxy = true;
                    mInitProxyLock.notifyAll();
                }
            }
        });
    }

    private void waitingForInitProxy() {
        if (!mIsInitProxy) {
            synchronized (mInitProxyLock) {
                if (!mIsInitProxy) {
                    while (true) {
                        try {
                            mInitProxyLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mIsInitProxy) {
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class MainProcessService extends FDLService {
    }

    public static class IndependentProcessService extends FDLService {
    }
}
