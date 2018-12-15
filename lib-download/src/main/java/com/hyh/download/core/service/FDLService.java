package com.hyh.download.core.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.hyh.download.Command;
import com.hyh.download.IClient;
import com.hyh.download.IRequest;
import com.hyh.download.bean.TaskInfo;
import com.hyh.download.core.Constants;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.ServiceDownloadProxyImpl;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.utils.L;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/3/8.
 */

public class FDLService extends Service {

    private final Object mInitProxyLock = new Object();

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
                Thread thread = new Thread(r, "FDLService-Command");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    private final ConcurrentHashMap<Integer, IClient> mClients = new ConcurrentHashMap<>();

    private int maxSynchronousDownloadNum = 2;

    private IDownloadProxy mServiceProxy;

    private volatile boolean mIsInitProxy;

    private IRequest mAgent = new IRequest.Stub() {

        @Override
        public void fixDatabaseErrorStatus() throws RemoteException {
            waitingForInitProxy();
        }

        @Override
        public void onReceiveStartCommand(TaskInfo taskInfo) throws RemoteException {
            waitingForInitProxy();
        }

        @Override
        public void onReceivePauseCommand(TaskInfo taskInfo) throws RemoteException {
            waitingForInitProxy();
        }

        @Override
        public void onReceiveDeleteCommand(TaskInfo taskInfo) throws RemoteException {
            waitingForInitProxy();
        }

        @Override
        public void request(int pid, int command, TaskInfo request) throws RemoteException {
            waitingForInitProxy();
            if (command >= 0 && request != null) {
                mServiceProxy.enqueue(command, request);
            }
        }

        @Override
        public void operateDatabase(int pid, TaskInfo taskInfo) throws RemoteException {
            waitingForInitProxy();
            mServiceProxy.operateDatabase(taskInfo);
        }


        @Override
        public void register(int pid, IClient client) throws RemoteException {
            waitingForInitProxy();
            mClients.put(pid, client);
        }

        @Override
        public boolean isTaskEnqueue(String resKey) throws RemoteException {
            waitingForInitProxy();
            return mServiceProxy.isTaskEnqueue(resKey);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        L.d("bind service");
        if (intent != null) {
            maxSynchronousDownloadNum = intent.getIntExtra(Constants.MAX_SYNCHRONOUS_DOWNLOAD_NUM, 2);
            if (mServiceProxy != null) {
                mServiceProxy.setMaxSynchronousDownloadNum(maxSynchronousDownloadNum);
            }
            L.d("bind service maxSynchronousDownloadNum=" + maxSynchronousDownloadNum);
        }
        return mAgent.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TaskDatabaseHelper.getInstance().init(getApplicationContext());
        mServiceProxy = new ServiceDownloadProxyImpl(
                this.getApplicationContext(),
                mClients,
                maxSynchronousDownloadNum);
        initProxy();
    }

    private void initProxy() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mInitProxyLock) {
                    mServiceProxy.initProxy();
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
        if (intent != null) {
            final int command = intent.getIntExtra(Constants.COMMADN, Command.UNKNOWN);
            final TaskInfo request = intent.getParcelableExtra(Constants.REQUEST_INFO);
            if (command >= 0 && request != null) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mServiceProxy.enqueue(command, request);
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
    }

    public static class MainProcessService extends FDLService {
    }

    public static class IndependentProcessService extends FDLService {
    }
}
