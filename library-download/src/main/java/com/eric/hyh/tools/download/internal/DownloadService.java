package com.eric.hyh.tools.download.internal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.eric.hyh.tools.download.ICallback;
import com.eric.hyh.tools.download.IRequest;
import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/3/8.
 */

public class DownloadService extends Service {


    private ThreadPoolExecutor executor;

    private Map<String, TaskDBInfo> mTaskDBInfoContainer = new ConcurrentHashMap<>();


    private IDownloadAgent.IServiceDownloadAgent.IServiceDownloadAgent mServiceAgent;


    private IRequest agent = new IRequest.Stub() {
        @Override
        public void request(int command, TaskInfo request) throws RemoteException {
           /* int pid = android.os.Process.myPid();
            Log.d("tag===", "DownloadService: " + pid);*/
            if (command >= 0 && request != null) {
                mServiceAgent.enqueue(command, request);
            }
        }

        @Override
        public void requestOperateDB(TaskInfo taskInfo) throws RemoteException {
            String resKey = taskInfo.getResKey();
            TaskDBInfo taskDBInfo = mTaskDBInfoContainer.get(resKey);
            if (taskDBInfo == null) {
                taskDBInfo = new TaskDBInfo();
                mTaskDBInfoContainer.put(resKey, taskDBInfo);
            }
            Utils.DBUtil.getInstance(getApplicationContext()).operate(taskInfo, taskDBInfo, executor);
        }


        @Override
        public void registerAll(ICallback callback) throws RemoteException {
            mServiceAgent.setCallback(callback);
        }

        @Override
        public void correctDBErroStatus() throws RemoteException {
            Utils.DBUtil.getInstance(getApplicationContext()).correctDBErroStatus(getApplicationContext());
        }

        @Override
        public void unRegisterAll() throws RemoteException {
            mServiceAgent.setCallback(null);
            stopSelf();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return agent.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executor = new ThreadPoolExecutor(0
                , 1
                , 60L
                , TimeUnit.SECONDS
                , new LinkedBlockingQueue<Runnable>()
                , new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "DownloadService dbPool");
            }
        });
        mServiceAgent = new OkhttpServiceAgent(this.getApplicationContext(), executor, mTaskDBInfoContainer);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int command = intent.getIntExtra(Constans.COMMADN, Command.UNKNOW);
            TaskInfo request = intent.getParcelableExtra(Constans.REQUEST_SERVICE);
            if (command >= 0 && request != null) {
                mServiceAgent.enqueue(command, request);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
