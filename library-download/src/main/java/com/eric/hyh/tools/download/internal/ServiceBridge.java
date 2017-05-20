package com.eric.hyh.tools.download.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.eric.hyh.tools.download.ICallback;
import com.eric.hyh.tools.download.IRequest;
import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.google.gson.Gson;

import java.lang.reflect.Type;
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
    private int currentConnectionRetryTimes = 0;
    private volatile boolean connected;
    private volatile ServiceConnection connection;
    private Context context;
    private IRequest serviceAgent;
    private Callback callback;
    private Map<String, Callback> callbacks = new HashMap<>();
    private Map<String, Type> types = new HashMap<>();
    private Map<String, TaskCache> cacheTasks = new LinkedHashMap<>();
    private Map<String, TaskInfo> cacheDBTasks = new LinkedHashMap<>();
    private Map<String, Object> tags = new HashMap<>();
    private Executor executor;
    private Gson gson;

    public ServiceBridge(Context context, Executor executor) {
        this.context = context.getApplicationContext();
        this.executor = executor;
        this.gson = new Gson();
    }

    public void bindService() {
        if (connected) {
            return;
        }
        if (connection == null) {
            connection = getConnection();
        }
        Intent intent = new Intent(context, DownloadService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        context.startService(intent);
    }

    public void unBindService() {
        if (connection != null) {
            try {
                serviceAgent.unRegisterAll();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            context.unbindService(connection);
            connection = null;
            connected = false;
        }
    }

    public void request(int command, TaskInfo taskInfo) {
        request(command, taskInfo, null);
    }

    public <T> void request(int command, TaskInfo taskInfo, Callback<T> callback) {
        String resKey = taskInfo.getResKey();
        TaskCache taskCache = new TaskCache(command, taskInfo, callback);
        cacheTasks.put(resKey, taskCache);
        if (connected) {
            try {
                types.put(resKey, taskInfo.getTagType());
                tags.put(resKey, taskInfo.getTag());
                serviceAgent.request(command, taskInfo);
                cacheTasks.remove(resKey);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindService();
        }
    }


    public void requestOperateDB(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        cacheDBTasks.put(resKey, taskInfo);
        if (connected) {
            TaskInfo remove = cacheDBTasks.remove(resKey);
            try {
                serviceAgent.requestOperateDB(remove);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindService();
        }
    }


    public boolean correctDBErroStatus() {
        if (connected) {
            try {
                serviceAgent.correctDBErroStatus();
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
                if (currentConnectionRetryTimes++ >= MAX_CONNECTION_RETRY_TIMES) {
                    return false;
                }
                return correctDBErroStatus();
            }
        } else {
            if (currentConnectionRetryTimes++ >= MAX_CONNECTION_RETRY_TIMES) {
                return false;
            }
            bindService();
            return correctDBErroStatus();
        }
    }

    public <T> void setCallback(Callback<T> callBack) {
        this.callback = callBack;
    }


    private ICallback.Stub iCallback = new ICallback.Stub() {
        @SuppressWarnings("unchecked")
        @Override
        public void onCall(TaskInfo taskInfo) throws RemoteException {
            if (taskInfo == null) {
                return;
            }
            String resKey = taskInfo.getResKey();
            Callback singleCallback = callbacks.get(resKey);
            supplementTaskInfo(taskInfo);
            switch (taskInfo.getCurrentStatus()) {
                case State.PREPARE:
                    if (singleCallback != null) {
                        singleCallback.onPrepare(taskInfo);
                    }
                    if (callback != null) {
                        callback.onPrepare(taskInfo);
                    }
                    break;
                case State.START_WRITE:
                    if (singleCallback != null) {
                        singleCallback.onFirstFileWrite(taskInfo);
                    }
                    if (callback != null) {
                        callback.onFirstFileWrite(taskInfo);
                    }
                    break;
                case State.DOWNLOADING:
                    if (singleCallback != null) {
                        singleCallback.onDownloading(taskInfo);
                    }
                    if (callback != null) {
                        callback.onDownloading(taskInfo);
                    }
                    break;
                case State.WAITING_IN_QUEUE:
                    if (singleCallback != null) {
                        singleCallback.onWaitingInQueue(taskInfo);
                    }
                    if (callback != null) {
                        callback.onWaitingInQueue(taskInfo);
                    }
                    break;
                case State.WAITING_FOR_WIFI:
                    if (singleCallback != null) {
                        singleCallback.onWaitingForWifi(taskInfo);
                    }
                    if (callback != null) {
                        callback.onWaitingForWifi(taskInfo);
                    }
                    break;
                case State.PAUSE:
                    if (singleCallback != null) {
                        singleCallback.onPause(taskInfo);
                    }
                    if (callback != null) {
                        callback.onPause(taskInfo);
                    }
                    break;
                case State.DELETE:
                    if (singleCallback != null) {
                        singleCallback.onDelete(taskInfo);
                    }
                    if (callback != null) {
                        callback.onDelete(taskInfo);
                    }
                    break;
                case State.SUCCESS:
                    if (singleCallback != null) {
                        singleCallback.onSuccess(taskInfo);
                    }
                    if (callback != null) {
                        callback.onSuccess(taskInfo);
                    }
                    break;
                case State.FAILURE:
                    if (singleCallback != null) {
                        singleCallback.onFailure(taskInfo);
                    }
                    if (callback != null) {
                        callback.onFailure(taskInfo);
                    }
                    break;
                case State.INSTALL:
                    if (singleCallback != null) {
                        singleCallback.onInstall(taskInfo);
                    }
                    if (callback != null) {
                        callback.onInstall(taskInfo);
                    }
                    break;
                case State.UNINSTALL:
                    if (singleCallback != null) {
                        singleCallback.onUnInstall(taskInfo);
                    }
                    if (callback != null) {
                        callback.onUnInstall(taskInfo);
                    }
                    break;
            }
        }

        @Override
        public void onHaveNoTask() throws RemoteException {
            if (callback != null) {
                callback.onHaveNoTask();
            }
        }
    };

    @SuppressWarnings("unchecked")
    private void supplementTaskInfo(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        Type type = types.get(resKey);
        Object tag = tags.get(resKey);
        taskInfo.setTagType(type);
        taskInfo.setTag(tag);
    }


    private ServiceConnection getConnection() {
        return connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceAgent = IRequest.Stub.asInterface(service);
                connected = true;
                try {
                    serviceAgent.registerAll(iCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (cacheTasks.size() > 0) {
                    Collection<TaskCache> values = cacheTasks.values();
                    for (TaskCache taskCache : values) {
                        request(taskCache.command, taskCache.taskInfo, taskCache.callback);
                    }
                }
                if (cacheDBTasks.size() > 0) {
                    Collection<TaskInfo> values = cacheDBTasks.values();
                    for (TaskInfo taskInfo : values) {
                        requestOperateDB(taskInfo);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }
}
