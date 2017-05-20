package com.eric.hyh.tools.download.api;

import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.IDownloadAgent;
import com.eric.hyh.tools.download.internal.OkhttpLocalAgent;
import com.eric.hyh.tools.download.internal.ServiceBridge;
import com.eric.hyh.tools.download.internal.Utils;
import com.eric.hyh.tools.download.internal.db.bean.TaskDBInfo;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.eric.hyh.tools.download.internal.Utils.deleteDownloadFile;


/**
 * Created by Administrator on 2017/3/8.
 */
@SuppressWarnings("unchecked")
public class FileDownloader {

    private static FileDownloader sFileDownloader;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(0//单线程池，用于操作数据库
            , 1
            , 60L
            , TimeUnit.SECONDS
            , new LinkedBlockingQueue<Runnable>()
            , new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "FileDownloader dbPool");
        }
    });

    private Gson gson = new Gson();

    private Context context;

    private IDownloadAgent.ILocalDownloadAgent mLocalAgent;//下载代理类

    private ServiceBridge mServiceBridge;//远程服务操作类

    private Map<String, FileCall> fileCalls = new ConcurrentHashMap<>();//存储当前正在进行的任务

    private Utils.DBUtil dbUtil;//数据库操作工具类

    private volatile boolean isExit = false;//应用是否已退出

    private boolean isForceDestroy = false;

    private volatile boolean isHaveNoTask = true;//是否有下载任务正在进行

    private volatile boolean isRelease = false;//内存是否已释放

    private volatile boolean correctDBErroStatus = false;//是否已矫正数据库中所存储任务的异常状态

    private List<Callback> listeners = new ArrayList<>();//存储外部注册的回调

    private Map<String, TaskDBInfo> tasks = new ConcurrentHashMap<>();//数据库中所有任务的列表

    private final DownloadListener mListener;//下载过程的回调


    public static FileDownloader getInstance(Context context) {
        if (sFileDownloader != null) {
            return sFileDownloader;
        }
        synchronized (FileDownloader.class) {
            if (sFileDownloader == null) {
                sFileDownloader = new FileDownloader(context);
            }
            return sFileDownloader;
        }
    }

    private FileDownloader(Context context) {
      /*  int pid = android.os.Process.myPid();
        Log.d("tag===", "FileDownloader: " + pid);*/
        this.context = context.getApplicationContext();
        dbUtil = Utils.DBUtil.getInstance(this.context);
        mServiceBridge = new ServiceBridge(this.context, executor);
        mListener = new DownloadListener();
        mLocalAgent = new OkhttpLocalAgent(this.context, mServiceBridge);
        mServiceBridge.bindService();
        mLocalAgent.setCallback(mListener);
        mServiceBridge.setCallback(mListener);
        correctDBErroStatus();
        initTasks();
    }

    private void initFileDownloader() {
        if (!isRelease) {
            return;
        }
        synchronized (FileDownloader.class) {
            if (isRelease) {
                mServiceBridge.bindService();
                correctDBErroStatus();
                isRelease = false;
            }
        }
    }


    private void correctDBErroStatus() {
        if (!isHaveNoTask) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                correctDBErroStatus = mServiceBridge.correctDBErroStatus();
                if (!correctDBErroStatus) {
                    correctDBErroStatus = dbUtil.correctDBErroStatus(context);
                }
            }
        });
    }


    private void initTasks() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                tasks = dbUtil.getAllTasks(tasks);
            }
        });
    }


    private ThreadPoolExecutor buildExecutor() {
        return new ThreadPoolExecutor(0//单线程池，用于操作数据库
                , 1
                , 60L
                , TimeUnit.SECONDS
                , new LinkedBlockingQueue<Runnable>()
                , new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "FileDownloader dbPool");
            }
        });
    }


    public <T> boolean startTask(FileRequest<T> fileRequest) {
        FileCall<T> fileCall = newCall(fileRequest, null);
        if (fileCall != null) {
            fileCall.fileRequest().changeCommand(Command.START);
            fileCall.enqueue();
            return true;
        } else {
            if (fileCalls.isEmpty()) {
                //TODO 没任务了
                mListener.onHaveNoTask();
            }
            return false;
        }
    }

    public <T> boolean startTask(FileRequest<T> fileRequest, Callback<T> callback) {
        FileCall<T> fileCall = newCall(fileRequest, callback);
        if (fileCall != null) {
            fileCall.fileRequest().changeCommand(Command.START);
            fileCall.enqueue(callback);
            return true;
        } else {
            if (fileCalls.isEmpty()) {
                //TODO 没任务了
                mListener.onHaveNoTask();
            }
            return false;
        }
    }


    public <T> void startWaitingForWifiTasks() {
        getSaveInDBWaitingForWifiTasksAsynch(new SearchListener<List<TaskInfo<T>>>() {
            @Override
            public void onResult(List<TaskInfo<T>> result) {
                if (result != null && !result.isEmpty()) {
                    for (TaskInfo<T> taskInfo : result) {
                        FileRequest<T> fileRequest = new FileRequest.Builder<T>() {
                        }
                                .tag(taskInfo.getTag())
                                .type(taskInfo.getTagType())
                                .key(taskInfo.getResKey())
                                .url(taskInfo.getUrl())
                                .packageName(taskInfo.getPackageName())
                                .fileSize(taskInfo.getTotalSize())
                                .versionCode(taskInfo.getVersionCode())
                                .byService(taskInfo.isByService())
                                .wifiAutoRetry(taskInfo.isWifiAutoRetry())
                                .build();
                        startTask(fileRequest);
                    }
                }
            }
        });
    }


    public <T> PendingIntent buildPendingIntent(FileRequest<T> fileRequest, Callback<T> callback) {
        /*initFileDownloader();
        FileCall<T> fileCall = newCall(fileRequest, callback);
        if (fileCall != null) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(Constans.COMMADN, fileRequest.command());
            intent.putExtra(Constans.COMMADN, fileCall.taskInfo());
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }*/
        return null;
    }

    public void deleteTask(final String resKey) {
        initFileDownloader();
        FileCall fileCall = fileCalls.remove(resKey);
        if (fileCall != null) {
            fileCall.fileRequest().changeCommand(Command.DELETE);
            fileCall.enqueue();
        } else {
            if (!tasks.isEmpty()) {
                TaskDBInfo taskDBInfo = tasks.get(resKey);
                if (taskDBInfo != null) {
                    mListener.onDelete(TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, gson));
                    deleteDownloadFile(context, resKey);
                }
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        TaskDBInfo taskDBInfo = dbUtil.getTaskDBInfoByResKey(resKey);
                        if (taskDBInfo != null) {
                            mListener.onDelete(TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, gson));
                            deleteDownloadFile(context, resKey);
                        }
                    }
                });
            }
        }
    }

    public void pauseTask(String resKey) {
        initFileDownloader();
        FileCall fileCall = fileCalls.remove(resKey);
        if (fileCall != null) {
            fileCall.fileRequest().changeCommand(Command.PAUSE);
            fileCall.enqueue();
        }
    }

    public void deleteTasks(List<String> resKeys) {
        for (String resKey : resKeys) {
            deleteTask(resKey);
        }
    }


    public void pauseTasks(List<String> resKeys) {
        for (String resKey : resKeys) {
            pauseTask(resKey);
        }
    }

    public void deleteAllTasks() {
        Set<String> resKeys = fileCalls.keySet();
        for (String resKey : resKeys) {
            deleteTask(resKey);
        }
    }

    public void pauseAllTasks() {
        Set<String> resKeys = fileCalls.keySet();
        for (String resKey : resKeys) {
            pauseTask(resKey);
        }
    }

    public <T> void addListener(Callback<T> callback) {
        listeners.add(callback);
    }

    public <T> void removeListener(Callback<T> callback) {
        listeners.remove(callback);
    }

    public <T> void removeAllListener() {
        listeners.clear();
    }


    public <T> List<TaskInfo<T>> getSaveInDBWaitingForWifiTasksSynch() {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = dbUtil.getWaitingForWifiTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, gson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }


    public <T> void getSaveInDBWaitingForWifiTasksAsynch(final SearchListener<List<TaskInfo<T>>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBWaitingForWifiTasksSynch();
                callback.onResult(taskInfos);
            }
        });
    }


    public <T> List<TaskInfo<T>> getSaveInDBWaitingForWifiTasksSynch(Type tagType) {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = dbUtil.getWaitingForWifiTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, gson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }


    public <T> List<TaskInfo<T>> getSaveInDBSuccessTasksSynch(Type tagType) {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = dbUtil.getSuccessTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, gson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public <T> List<TaskInfo<T>> getSaveInDBInstalledTasksSynch(Type tagType) {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = dbUtil.getInstalledTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, gson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public <T> List<TaskInfo<T>> getAllAppsSynch(Type tagType) {
        if (!correctDBErroStatus && isHaveNoTask) {
            correctDBErroStatus = mServiceBridge.correctDBErroStatus();
            if (!correctDBErroStatus) {
                correctDBErroStatus = dbUtil.correctDBErroStatus(context);
            }
        }
        List<TaskDBInfo> list = dbUtil.getAllTasks();
        List<TaskInfo<T>> taskInfos = new ArrayList<>();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, gson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }


    public <T> void getSaveInDBWaitingForWifiTasksAsynch(final Type type, final SearchListener<List<TaskInfo<T>>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBWaitingForWifiTasksSynch(type);
                callback.onResult(taskInfos);
            }
        });
    }


    public <T> void getSaveInDBSuccessTasksAsynch(final Type type, final SearchListener<List<TaskInfo<T>>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBSuccessTasksSynch(type);
                callback.onResult(taskInfos);
            }
        });

    }


    public <T> void getSaveInDBInstalledTasksAsynch(final Type type, final SearchListener<List<TaskInfo<T>>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBInstalledTasksSynch(type);
                callback.onResult(taskInfos);
            }
        });

    }


    public <T> void getAllAppsAsynch(final Type tagType, final SearchListener<List<TaskInfo<T>>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getAllAppsSynch(tagType);
                callback.onResult(taskInfos);
            }
        });
    }


    private <T> FileCall<T> newCall(final FileRequest<T> request, Callback<T> callback) {
        initFileDownloader();
        isHaveNoTask = false;
        final String resKey = request.key();
        File file = Utils.getDownLoadFile(context, request.key());//获取已下载文件
        long currentSize = 0;
        if (file.exists()) {
            currentSize = file.length();
        }
        TaskInfo<T> taskInfo = generateTaskInfo(request, file, currentSize);
        FileCall fileCall = fileCalls.get(resKey);
        if (fileCall != null) {
            boolean byService = fileCall.fileRequest().byService();
            if (byService != request.byService()) {
                return null;
            } else {
                fileCalls.remove(resKey);
            }
            fileCall = new FileCall(request, mLocalAgent, mServiceBridge, taskInfo);
            fileCalls.put(resKey, fileCall);
            return fileCall;
        }
        //校验之前下载的文件
        checkOldFile(request, resKey, taskInfo);

        if (!hasEnoughDiskSpace(request.fileSize())) {//判断磁盘空间大小
            //TODO 磁盘空间不足
            if (callback != null) {
                callback.onNoEnoughSpace(taskInfo);
            }
            mListener.onNoEnoughSpace(taskInfo);
            return null;
        }


        boolean isUpdate = request.command() == Command.UPDATE;
        if (isUpdate) {//更新指令
            fileCall = new FileCall(request, mLocalAgent, mServiceBridge, taskInfo);
            fileCalls.put(resKey, fileCall);
            return fileCall;
        }


        String packageName = request.packageName();
        boolean hasPackageName = !TextUtils.isEmpty(packageName);
        if (hasPackageName) {
            int oldVersionCode = Utils.getVersionCode(context, request.packageName());
            if (oldVersionCode > 0) {//应用已安装
                int versionCode = request.versionCode();
                if (versionCode == oldVersionCode) {//应用不需要更新
                    taskInfo.setCurrentStatus(State.INSTALL);
                    if (callback != null) {
                        callback.onInstall(taskInfo);
                    }
                    mListener.onInstall(taskInfo);
                    mServiceBridge.requestOperateDB(taskInfo);
                    return null;
                } else {
                    if (currentSize > 0 && request.fileSize() == currentSize) {
                        //TODO 下载成功
                        taskInfo.setCurrentStatus(State.SUCCESS);
                        if (callback != null) {
                            callback.onSuccess(taskInfo);
                        }
                        mListener.onSuccess(taskInfo);
                        mServiceBridge.requestOperateDB(taskInfo);
                        return null;
                    }
                }
            } else {//应用未安装
                if (currentSize > 0 && request.fileSize() == currentSize) {
                    //TODO 下载成功
                    taskInfo.setCurrentStatus(State.SUCCESS);
                    if (callback != null) {
                        callback.onSuccess(taskInfo);
                    }
                    mListener.onSuccess(taskInfo);
                    mServiceBridge.requestOperateDB(taskInfo);
                    return null;
                }
            }
        } else {
            long requestFileSize = request.fileSize();
            TaskDBInfo taskDBInfo = tasks.isEmpty() ? dbUtil.getTaskDBInfoByResKey(resKey) : tasks.get(resKey);
            if (requestFileSize == 0) {
                if (taskDBInfo != null) {
                    requestFileSize = taskDBInfo.getTotalSize();
                }
            }
            if (currentSize == 0
                    && taskDBInfo != null
                    && !TextUtils.isEmpty(taskDBInfo.getPackageName())
                    && Utils.isAppInstall(context, taskDBInfo.getPackageName())) {
                //TODO 已经安装
                taskInfo.setCurrentStatus(State.INSTALL);
                if (callback != null) {
                    callback.onInstall(taskInfo);
                }
                mListener.onInstall(taskInfo);
                if (taskDBInfo.getCurrentStatus() == null || taskDBInfo.getCurrentStatus() != State.INSTALL) {
                    mServiceBridge.requestOperateDB(taskInfo);
                }
                return null;
            } else if (currentSize > 0 && requestFileSize == currentSize) {
                //TODO 下载成功
                taskInfo.setCurrentStatus(State.SUCCESS);
                if (callback != null) {
                    callback.onSuccess(taskInfo);
                }
                mListener.onSuccess(taskInfo);
                if (taskDBInfo.getCurrentStatus() == null || taskDBInfo.getCurrentStatus() != State.SUCCESS) {
                    mServiceBridge.requestOperateDB(taskInfo);
                }
                return null;
            }
        }

        fileCall = new FileCall(request, mLocalAgent, mServiceBridge, taskInfo);
        fileCalls.put(resKey, fileCall);
        return fileCall;
    }

    private <T> void checkOldFile(FileRequest<T> request, String resKey, TaskInfo<T> taskInfo) {//校验之前下载的文件
        int saveVersionCode = -1;
        long saveFileTotalSize = 0;
        if (tasks.isEmpty()) {
            TaskDBInfo taskDBInfo = dbUtil.getTaskDBInfoByResKey(resKey);
            if (taskDBInfo != null) {
                Integer versionCode = taskDBInfo.getVersionCode();//获取数据库中存储的版本号
                saveVersionCode = (versionCode == null ? -1 : versionCode);
                saveFileTotalSize = (taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
                tasks.put(taskDBInfo.getResKey(), taskDBInfo);
            }
        } else {
            TaskDBInfo taskDBInfo = tasks.get(resKey);
            if (taskDBInfo != null) {
                saveVersionCode = (taskDBInfo.getVersionCode() == null ? -1 : taskDBInfo.getVersionCode());
                saveFileTotalSize = (taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
            }
        }
        if ((request.versionCode() > 0 && (request.versionCode() != saveVersionCode))
                || (request.fileSize() > 0 && (request.fileSize() != saveFileTotalSize))) {
            boolean delete = deleteDownloadFile(context, resKey);
            if (delete) {
                taskInfo.setProgress(0);
                taskInfo.setCurrentSize(0);
            }
        }
    }

    private boolean hasEnoughDiskSpace(long fileSize) {
        Collection<FileCall> values = fileCalls.values();
        for (FileCall value : values) {
            fileSize += value.fileRequest().fileSize();
        }
        if (Utils.externalMemoryAvailable()) {
            long availableSize = Utils.getAvailableExternalMemorySize();
            if (availableSize > fileSize) {
                return true;
            }
        } else {
            long availableSize = Utils.getAvailableInternalMemorySize();
            if (availableSize > fileSize) {
                return true;
            }
        }
        return false;
    }

    private <T> TaskInfo<T> generateTaskInfo(FileRequest<T> request, File file, Long currentSize) {
        TaskInfo taskInfo = new TaskInfo();
        if (request.fileSize() > 0) {
            taskInfo.setProgress((int) ((currentSize * 100.0 / request.fileSize()) + 0.5));
        }
        taskInfo.setFilePath(file.getPath());
        taskInfo.setCurrentSize(currentSize);
        taskInfo.setResKey(request.key());
        taskInfo.setUrl(request.url());
        taskInfo.setTotalSize(request.fileSize());
        taskInfo.setVersionCode(request.versionCode());
        taskInfo.setByService(request.byService());
        taskInfo.setPackageName(request.packageName());
        taskInfo.setWifiAutoRetry(request.wifiAutoRetry());
        T tag = request.tag();
        Type type = request.type();
        String tagClassName = null;
        if (type != null) {
            String typeStr = type.toString();
            if (!TextUtils.isEmpty(typeStr) && typeStr.length() > 6) {
                tagClassName = typeStr.substring(6);
            }
        }
        taskInfo.setTagClassName(tagClassName);
        if (type != null && tag != null) {
            taskInfo.setTagJson(gson.toJson(tag, type));
        }
        taskInfo.setTag(tag);
        taskInfo.setTagType(type);
        return taskInfo;
    }

    private class DownloadListener implements Callback {


        @Override
        public void onNoEnoughSpace(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onPrepare(taskInfo);
                }
            }
        }

        @Override
        public void onPrepare(TaskInfo taskInfo) {
            tasks.put(taskInfo.getResKey(), TaskInfo.taskInfo2TaskDBInfo(taskInfo));
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onPrepare(taskInfo);
                }
            }
        }

        @Override
        public void onFirstFileWrite(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onFirstFileWrite(taskInfo);
                }
            }
        }

        @Override
        public void onDownloading(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onDownloading(taskInfo);
                }
            }
        }

        @Override
        public void onWaitingInQueue(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onWaitingInQueue(taskInfo);
                }
            }
        }

        @Override
        public void onWaitingForWifi(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onWaitingForWifi(taskInfo);
                }
            }
        }

        @Override
        public void onDelete(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            tasks.remove(resKey);
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onDelete(taskInfo);
                }
            }
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onPause(taskInfo);
                }
            }
        }

        @Override
        public void onSuccess(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            fileCalls.remove(taskInfo.getResKey());
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onSuccess(taskInfo);
                }
            }
        }

        @Override
        public void onInstall(TaskInfo taskInfo) {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onInstall(taskInfo);
                }
            }
        }

        @Override
        public void onUnInstall(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            tasks.remove(resKey);
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onUnInstall(taskInfo);
                }
            }
        }

        @Override
        public void onFailure(TaskInfo taskInfo) {
            fileCalls.remove(taskInfo.getResKey());
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onFailure(taskInfo);
                }
            }
        }

        @Override
        public void onHaveNoTask() {
            for (Callback listener : listeners) {
                if (listener != null) {
                    listener.onHaveNoTask();
                }
            }
            isHaveNoTask = true;
            if (Utils.isAppExit(context)) {
                release();
            } else {
                isExit = false;
            }
        }
    }


    public void onDestory() {
        isExit = true;
        if (isHaveNoTask) {
            release();
        }
    }

    public void forceDestory() {
        isForceDestroy = true;
        if (isHaveNoTask) {
            release();
        }
    }


    private void release() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (isForceDestroy && isHaveNoTask) {
                    mServiceBridge.unBindService();
                    isRelease = true;
                    isForceDestroy = false;
                } else {
                    SystemClock.sleep(32 * 1000);
                    if (Utils.isAppExit(context)) {
                        isExit = true;
                    } else {
                        isExit = false;
                    }
                    if (isExit && isHaveNoTask) {
                        mServiceBridge.unBindService();
                        isRelease = true;
                    }
                }
            }
        });
    }


    public interface SearchListener<T> {
        void onResult(T result);
    }

    public void onInstall(TaskDBInfo taskDBInfo) {
        TaskInfo taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, gson);
        taskInfo.setCurrentStatus(State.INSTALL);
        mListener.onInstall(taskInfo);
        if (fileCalls.isEmpty()) {
            //TODO 没任务了
            mListener.onHaveNoTask();
        }
    }

    public void onUnInstall(TaskDBInfo taskDBInfo) {
        TaskInfo taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, gson);
        taskInfo.setCurrentStatus(State.UNINSTALL);
        mListener.onUnInstall(taskInfo);
        if (fileCalls.isEmpty()) {
            //TODO 没任务了
            mListener.onHaveNoTask();
        }
    }
}
