package com.hyh.download.core.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.hyh.download.FileChecker;
import com.hyh.download.IClient;
import com.hyh.download.IFileChecker;
import com.hyh.download.IRequest;
import com.hyh.download.bean.TaskInfo;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.ServiceDownloadProxyImpl;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.utils.L;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/3/8.
 */

public class FDLService extends Service {

    private final Object mInitProxyLock = new Object();

    private final ConcurrentHashMap<Integer, IClient> mClients = new ConcurrentHashMap<>();

    private IDownloadProxy mServiceProxy;

    private volatile boolean mIsInitProxy;

    private IRequest mAgent = new IRequest.Stub() {

        @Override
        public boolean isAlive() throws RemoteException {
            return true;
        }

        @Override
        public void register(int pid, IClient client) throws RemoteException {
            waitingForInitProxy();
            mClients.put(pid, client);
        }

        @Override
        public void initDownloadProxy(int maxSynchronousDownloadNum) throws RemoteException {
            if (mServiceProxy != null) {
                mServiceProxy = new ServiceDownloadProxyImpl(
                        getApplicationContext(),
                        mClients,
                        maxSynchronousDownloadNum);
                initProxy();
            }
            waitingForInitProxy();
        }

        @Override
        public void onReceiveStartCommand(String resKey) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.onReceiveStartCommand(resKey);
        }

        @Override
        public void onReceivePauseCommand(String resKey) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.onReceivePauseCommand(resKey);
        }

        @Override
        public void onReceiveDeleteCommand(String resKey) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.onReceiveDeleteCommand(resKey);
        }

        @Override
        public void insertOrUpdate(TaskInfo taskInfo) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.insertOrUpdate(taskInfo);
        }

        @Override
        public boolean isTaskAlive(String resKey) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.isTaskAlive(resKey);
        }

        @Override
        public boolean isFileDownloaded(String resKey) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.isFileDownloaded(resKey);
        }

        @Override
        public TaskInfo getTaskInfoByKey(String resKey) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.getTaskInfoByKey(resKey);
        }

        @Override
        public void startTask(TaskInfo taskInfo, IFileChecker fileChecker) throws RemoteException {
            waitingForInitProxy();
            FileCheckerWrapper fileCheckerWrapper = null;
            if (fileChecker != null) {
                fileCheckerWrapper = new FileCheckerWrapper(fileChecker);
            }
            mServiceProxy.startTask(taskInfo, fileCheckerWrapper);
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
        TaskDatabaseHelper.getInstance().init(getApplicationContext());
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

    private static class FileCheckerWrapper implements FileChecker {

        private IFileChecker fileChecker;

        public FileCheckerWrapper(IFileChecker fileChecker) {
            this.fileChecker = fileChecker;
        }

        @Override
        public boolean isValidFile(TaskInfo taskInfo) {
            if (fileChecker == null) {
                return true;
            }
            try {
                return fileChecker.isValidFile(taskInfo);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean isRetryDownload() {
            if (fileChecker == null) {
                return false;
            }
            try {
                return fileChecker.isRetryDownload();
            } catch (Exception e) {
                return false;
            }
        }
    }
}
