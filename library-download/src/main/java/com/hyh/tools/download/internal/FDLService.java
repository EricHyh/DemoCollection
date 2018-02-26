package com.hyh.tools.download.internal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hyh.tools.download.IClient;
import com.hyh.tools.download.IRequest;
import com.hyh.tools.download.bean.Command;
import com.hyh.tools.download.bean.Constants;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.internal.db.bean.TaskDBInfo;
import com.hyh.tools.download.utils.DBUtil;
import com.hyh.tools.download.utils.Utils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2017/3/8.
 */

public class FDLService extends Service {


    private ThreadPoolExecutor mCommandExecutor;

    private final ConcurrentHashMap<Integer, IClient> mClients = new ConcurrentHashMap<>();

    private int maxSynchronousDownloadNum = 2;

    private IDownloadProxy.IServiceDownloadProxy mServiceProxy;


    private IRequest mAgent = new IRequest.Stub() {
        @Override
        public void request(int pid, int command, TaskInfo request) throws RemoteException {
            if (command >= 0 && request != null) {
                mServiceProxy.enqueue(command, request);
            }
        }

        @Override
        public void onCall(int pid, TaskInfo taskInfo) throws RemoteException {
            Set<Integer> pids = mClients.keySet();
            pids.remove(pid);
            if (!pids.isEmpty()) {
                for (Integer otherPid : pids) {
                    IClient iClient = mClients.get(otherPid);
                    iClient.onCall(taskInfo);
                }
            }
            DBUtil.getInstance(getApplicationContext()).operate(taskInfo);
        }


        @Override
        public void register(int pid, IClient client) throws RemoteException {
            mClients.put(pid, client);
            if (mClients.size() > 1) {
                Collection<IClient> values = mClients.values();
                for (IClient iClient : values) {
                    iClient.onProcessChanged(true);
                }
            }
        }


        @Override
        public void unRegister(int pid) throws RemoteException {
            mClients.remove(pid);
            if (mClients.isEmpty()) {
                stopSelf();
            } else if (mClients.size() == 1) {
                Collection<IClient> values = mClients.values();
                for (IClient iClient : values) {
                    iClient.onProcessChanged(false);
                }
            }
        }

        @Override
        public boolean isFileDownloading(int pid, String resKey) throws RemoteException {
            Set<Integer> pids = mClients.keySet();
            for (Integer otherPid : pids) {
                if (pid == otherPid) {
                    continue;
                }
                IClient iClient = mClients.get(otherPid);
                if (iClient.isFileDownloading(resKey)) {
                    return true;
                }
            }
            return false;
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("FDL_HH==", "bind service");
        if (intent != null) {
            maxSynchronousDownloadNum = intent.getIntExtra(Constants.MAX_SYNCHRONOUS_DOWNLOAD_NUM, 2);
            if (mServiceProxy != null) {
                mServiceProxy.setMaxSynchronousDownloadNum(maxSynchronousDownloadNum);
            }
            Log.d("FDL_HH", "bind service maxSynchronousDownloadNum=" + maxSynchronousDownloadNum);
        }
        return mAgent.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceProxy = new ServiceDownloadProxyImpl(
                this.getApplicationContext(),
                mClients,
                maxSynchronousDownloadNum);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final int command = intent.getIntExtra(Constants.COMMADN, Command.UNKNOW);
            final TaskInfo request = intent.getParcelableExtra(Constants.REQUEST_INFO);
            if (command >= 0 && request != null) {
                if (mCommandExecutor == null) {
                    mCommandExecutor = Utils.buildExecutor(1, 1, 120, "FDLService Command Thread", true);
                }
                final String resKey = request.getResKey();
                final Collection<IClient> values = mClients.values();
                mCommandExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean isFileDownloading = false;
                            IClient downloadClient = null;
                            for (IClient value : values) {
                                if (value.isFileDownloading(request.getResKey())) {
                                    downloadClient = value;
                                    isFileDownloading = true;
                                }
                            }
                            if (isFileDownloading) {
                                if (command == Command.PAUSE || command == Command.DELETE) {
                                    downloadClient.otherProcessCommand(command, resKey);
                                }
                            } else {
                                if (command == Command.UPDATE || command == Command.START) {
                                    mServiceProxy.enqueue(command, request);
                                }
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCommandExecutor != null && !mCommandExecutor.isShutdown()) {
            mCommandExecutor.shutdown();
        }
    }

    public static class MainProcessService extends FDLService {
    }

    public static class IndependentProcessService extends FDLService {
    }
}
