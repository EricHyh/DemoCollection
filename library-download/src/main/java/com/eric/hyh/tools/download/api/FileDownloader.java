package com.eric.hyh.tools.download.api;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;
import com.eric.hyh.tools.download.internal.Constans;
import com.eric.hyh.tools.download.internal.DownloadProxyFactory;
import com.eric.hyh.tools.download.internal.FDLService;
import com.eric.hyh.tools.download.internal.IDownloadProxy;
import com.eric.hyh.tools.download.internal.OkhttpLocalProxy;
import com.eric.hyh.tools.download.internal.ServiceBridge;
import com.eric.hyh.tools.download.internal.SingleTaskListenerManager;
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
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2017/3/8.
 */
@SuppressWarnings("unchecked")
public class FileDownloader implements DownloadProxyFactory {

    private static final long KEEP_ALIVE_TIME_OUTAPP = 30 * 1000;

    private static final long KEEP_ALIVE_TIME_INAPP = 60 * 1000;

    @SuppressLint("StaticFieldLeak")
    private static FileDownloader sFileDownloader;

    private final int multiThreadNum;

    private ThreadPoolExecutor mExecutor = Utils.buildExecutor(4, 4, 60, "FileDownload Thread", true);

    private Gson mGson = new Gson();

    private final LockConfig mLockConfig = new LockConfig();

    private Context mContext;

    private IDownloadProxy.ILocalDownloadProxy mDownloadProxy;//本地下载代理类

    private ConcurrentHashMap<String, FileCall> mFileCalls = new ConcurrentHashMap<>();//存储当前正在进行的任务

    private Utils.DBUtil mDBUtil;//数据库操作工具类

    private volatile boolean haveNoTask = true;//是否有下载任务正在进行

    private volatile boolean released = false;//内存是否已释放

    private volatile boolean releaseStarted;//是否开始释放内存的任务

    private List<Callback> mListeners = new ArrayList<>();//存储外部注册的回调

    private Map<String, TaskDBInfo> mHistoryTasks = new ConcurrentHashMap<>();//数据库中所有任务的列表

    private final SingleTaskListenerManager mListenerManager;

    public static void initFileDownloader(Context context) {
        initFileDownloader(context, true, 2);
    }

    public static void initFileDownloader(Context context, boolean byService, int maxSynchronousDownloadNum) {
        if (sFileDownloader != null) {
            return;
        }
        synchronized (FileDownloader.class) {
            if (sFileDownloader == null) {
                sFileDownloader =
                        new FileDownloader(new FileDownloaderBuilder(context.getApplicationContext(), byService, maxSynchronousDownloadNum));
            }
        }
    }

    public static FileDownloader getInstance() {
        return sFileDownloader;
    }

    private FileDownloader(FileDownloaderBuilder builder) {
        if (builder == null) {
            throw new NullPointerException("Context is null, please init FileDownloader!!");
        }
        this.mContext = builder.context;
        this.multiThreadNum = Utils.computeMultiThreadNum(builder.maxSynchronousDownloadNum);
        this.mDBUtil = Utils.DBUtil.getInstance(this.mContext);
        this.mListenerManager = new SingleTaskListenerManager(new DownloadListener());
        this.mDownloadProxy = produce(builder.byService, builder.maxSynchronousDownloadNum);

        mDownloadProxy.setAllTaskCallback(mListenerManager);
        mDownloadProxy.initProxy(mLockConfig);

        initHistoryTasks();
    }

    private void initHistoryTasks() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                waitInitProxyFinish();
                mHistoryTasks = mDBUtil.getAllTaskMap();
                synchronized (mLockConfig) {
                    mLockConfig.setInitHistoryFinish(true);
                    mLockConfig.notifyAll();
                }
            }
        });
    }

    public <T> void startTask(FileRequest<T> fileRequest) {
        startTask(fileRequest, null);
    }

    public <T> void startTask(final FileRequest<T> fileRequest, final Callback<T> callback) {

        int activeCount = mExecutor.getActiveCount();
        int corePoolSize = mExecutor.getCorePoolSize();
        long taskCount = mExecutor.getTaskCount();
        int size = mExecutor.getQueue().size();

        Log.d("FDL_HH", "startTask: activeCount="+activeCount);
        Log.d("FDL_HH", "startTask: corePoolSize="+corePoolSize);
        Log.d("FDL_HH", "startTask: taskCount="+taskCount);
        Log.d("FDL_HH", "startTask: getQueue size="+size);

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileCall<T> fileCall = newCall(fileRequest, callback);
                if (fileCall != null) {
                    fileCall.fileRequest().changeCommand(Command.START);
                    if (callback != null) {
                        mListenerManager.addSingleTaskCallback(fileRequest.key(), callback);
                    }
                    fileCall.enqueue();
                } else {
                    if (mFileCalls.isEmpty()) {
                        //TODO 没任务了
                        mListenerManager.onHaveNoTask();
                    }
                }
            }
        });
    }

    public <T> void startWaitingForWifiTasks() {
        getSaveInDBWaitingForWifiTasksAsynch(new SearchListener<List<TaskInfo<T>>>() {
            @Override
            public void onResult(List<TaskInfo<T>> result) {
                if (result != null && !result.isEmpty()) {
                    for (TaskInfo<T> taskInfo : result) {
                        FileRequest<T> fileRequest = new FileRequest.Builder<T>()
                                .tag(taskInfo.getTag())
                                .byMultiThread(taskInfo.getRangeNum() > 1)
                                .type(taskInfo.getTagType())
                                .key(taskInfo.getResKey())
                                .url(taskInfo.getUrl())
                                .packageName(taskInfo.getPackageName())
                                .fileSize(taskInfo.getTotalSize())
                                .versionCode(taskInfo.getVersionCode())
                                .wifiAutoRetry(taskInfo.isWifiAutoRetry())
                                .build();
                        startTask(fileRequest);
                    }
                }
            }
        });
    }

    public boolean isFileDownloading(String resKey) {
        return mFileCalls.get(resKey) != null;
    }

    public boolean isFileDownloaded(String resKey) {
        TaskDBInfo taskDBInfo = mDBUtil.getTaskDBInfoByResKey(resKey);
        return taskDBInfo != null && taskDBInfo.getCurrentStatus() == State.SUCCESS;
    }

    public <T> PendingIntent buildPendingIntent(FileRequest<T> fileRequest, Callback<T> callback) {
        String resKey = fileRequest.key();
        int command = fileRequest.command();
        FileCall fileCall = mFileCalls.get(resKey);
        if (fileCall != null) {
            if (command == Command.PAUSE || command == Command.DELETE) {
                mListenerManager.addSingleTaskCallback(resKey, callback);
                Intent intent = new Intent(mContext, FDLService.class);
                intent.putExtra(Constans.COMMADN, fileRequest.command());
                intent.putExtra(Constans.REQUEST_INFO, fileCall.taskInfo());
                return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        } else {
            if (command == Command.UPDATE || command == Command.START) {
                fileCall = newCall(fileRequest, callback);
                mListenerManager.addSingleTaskCallback(resKey, callback);
                if (fileCall != null) {
                    Intent intent = new Intent(mContext, FDLService.class);
                    intent.putExtra(Constans.COMMADN, fileRequest.command());
                    intent.putExtra(Constans.REQUEST_INFO, fileCall.taskInfo());
                    return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                }
            } else {
                mListenerManager.addSingleTaskCallback(resKey, callback);
                Intent intent = new Intent(mContext, FDLService.class);
                intent.putExtra(Constans.COMMADN, fileRequest.command());

                File file = Utils.getDownLoadFile(mContext, fileRequest.key());//获取已下载文件
                Object[] currentSizeAndMultiPositions = null;
                if (file.exists()) {
                    currentSizeAndMultiPositions = Utils.getCurrentSizeAndMultiPositions(mContext, fileRequest, file, mHistoryTasks.get(resKey));
                }
                TaskInfo<T> taskInfo = generateTaskInfo(fileRequest, file, currentSizeAndMultiPositions);


                intent.putExtra(Constans.REQUEST_INFO, taskInfo);
                return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
        return null;
    }

    public void deleteTask(final String resKey) {
        FileCall fileCall = mFileCalls.remove(resKey);
        if (fileCall != null) {
            fileCall.fileRequest().changeCommand(Command.DELETE);
            fileCall.enqueue();
        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    TaskDBInfo taskDBInfo = mDBUtil.getTaskDBInfoByResKey(resKey);
                    if (taskDBInfo != null) {
                        mListenerManager.onDelete(TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson));
                        Utils.deleteDownloadFile(mContext, resKey, taskDBInfo.getRangeNum() == null ? 0 : taskDBInfo.getRangeNum());
                    }
                }
            });
        }
    }

    public void pauseTask(String resKey) {
        FileCall fileCall = mFileCalls.remove(resKey);
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
        Set<String> resKeys = mFileCalls.keySet();
        for (String resKey : resKeys) {
            deleteTask(resKey);
        }
    }

    public void pauseAllTasks() {
        Set<String> resKeys = mFileCalls.keySet();
        for (String resKey : resKeys) {
            pauseTask(resKey);
        }
    }

    public <T> void addListener(Callback<T> callback) {
        mListeners.add(callback);
    }

    public <T> void removeListener(Callback<T> callback) {
        mListeners.remove(callback);
    }

    public <T> void removeAllListener() {
        mListeners.clear();
    }

    public <T> List<TaskInfo<T>> getSaveInDBWaitingForWifiTasksSynch() {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mDBUtil.getWaitingForWifiTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public <T> void getSaveInDBWaitingForWifiTasksAsynch(final SearchListener<List<TaskInfo<T>>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBWaitingForWifiTasksSynch();
                callback.onResult(taskInfos);
            }
        });
    }

    public <T> List<TaskInfo<T>> getSaveInDBWaitingForWifiTasksSynch(Type tagType) {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mDBUtil.getWaitingForWifiTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, mGson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public <T> List<TaskInfo<T>> getSaveInDBSuccessTasksSynch(Type tagType) {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mDBUtil.getSuccessTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, mGson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public <T> List<TaskInfo<T>> getSaveInDBInstalledTasksSynch(Type tagType) {
        ArrayList<TaskInfo<T>> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mDBUtil.getInstalledTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, mGson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public <T> List<TaskInfo<T>> getAllAppsSynch(Type tagType) {
        waitInitProxyFinish();
        List<TaskDBInfo> list = mDBUtil.getAllTaskList();
        List<TaskInfo<T>> taskInfos = new ArrayList<>();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo<T> taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, tagType, mGson);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    private void waitInitProxyFinish() {
        if (!mLockConfig.isInitProxyFinish) {
            synchronized (mLockConfig) {
                while (true) {
                    try {
                        mLockConfig.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mLockConfig.isInitProxyFinish) {
                        break;
                    }
                }
            }
        }
    }

    public <T> void getSaveInDBWaitingForWifiTasksAsynch(final Type type, final SearchListener<List<TaskInfo<T>>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBWaitingForWifiTasksSynch(type);
                callback.onResult(taskInfos);
            }
        });
    }

    public <T> void getSaveInDBSuccessTasksAsynch(final Type type, final SearchListener<List<TaskInfo<T>>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBSuccessTasksSynch(type);
                callback.onResult(taskInfos);
            }
        });

    }

    public <T> void getSaveInDBInstalledTasksAsynch(final Type type, final SearchListener<List<TaskInfo<T>>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getSaveInDBInstalledTasksSynch(type);
                callback.onResult(taskInfos);
            }
        });

    }

    public <T> void getAllAppsAsynch(final Type tagType, final SearchListener<List<TaskInfo<T>>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo<T>> taskInfos = getAllAppsSynch(tagType);
                callback.onResult(taskInfos);
            }
        });
    }

    private <T> FileCall<T> newCall(final FileRequest<T> request, Callback<T> callback) {
        final String resKey = request.key();
        FileCall fileCall = mFileCalls.get(resKey);
        if (fileCall != null) {
            return null;
        }

        if (!mLockConfig.isInitHistoryFinish) {
            synchronized (mLockConfig) {
                while (true) {
                    try {
                        mLockConfig.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mLockConfig.isInitHistoryFinish) {
                        break;
                    }
                }
            }
        }

        haveNoTask = false;


        // TODO: 2017/6/23 问下其他进程的兄弟有没有在下载这个任务
        boolean isFileDownloading = mDownloadProxy.isFileDownloading(resKey);
        if (isFileDownloading) {
            return null;
        }

        long startTime = System.currentTimeMillis();

        File file = Utils.getDownLoadFile(mContext, request.key());//获取已下载文件
        Object[] currentSizeAndMultiPositions = null;
        if (file.exists()) {
            currentSizeAndMultiPositions = Utils.getCurrentSizeAndMultiPositions(mContext, request, file, mHistoryTasks.get(resKey));
        }

        long endTime = System.currentTimeMillis();

        Log.d("FDL_HH", "newCall: getCurrentSizeAndMultiPositions time="+(endTime-startTime)/1000);

        TaskInfo<T> taskInfo = generateTaskInfo(request, file, currentSizeAndMultiPositions);


        //校验之前下载的文件版本
        checkOldFile(request, resKey, taskInfo);

        long currentSize = taskInfo.getCurrentSize();

        TaskDBInfo taskDBInfo = mHistoryTasks.isEmpty() ? mDBUtil.getTaskDBInfoByResKey(resKey) : mHistoryTasks.get(resKey);

        boolean isUpdate = request.command() == Command.UPDATE;
        boolean isAppInstall = false;
        if (!isUpdate) {//更新指令
            String packageName = request.packageName();
            if (TextUtils.isEmpty(packageName) && taskDBInfo != null) {
                packageName = taskDBInfo.getPackageName();
            }
            boolean hasPackageName = !TextUtils.isEmpty(packageName);
            int oldVersionCode = -1;
            if (hasPackageName) {
                oldVersionCode = Utils.getVersionCode(mContext, request.packageName());
                if (oldVersionCode > 0) {
                    isAppInstall = true;
                }
            } else {
                if (taskDBInfo != null) {
                    oldVersionCode = taskDBInfo.getVersionCode() == null ? -1 : taskDBInfo.getVersionCode();
                }
            }
            if (request.versionCode() > 0 && oldVersionCode > 0 && (oldVersionCode != request.versionCode())) {
                isUpdate = true;
            }
        }
        if (isUpdate) {
            if (!hasEnoughDiskSpace(request.fileSize())) {//判断磁盘空间大小
                //TODO 磁盘空间不足
                if (callback != null) {
                    callback.onNoEnoughSpace(taskInfo);
                }
                mListenerManager.onNoEnoughSpace(taskInfo);
                return null;
            }
            fileCall = new FileCall(request, mDownloadProxy, taskInfo);
            mFileCalls.put(resKey, fileCall);
            return fileCall;
        }
        if (isAppInstall) {
            //TODO 已经安装
            if (taskDBInfo == null) {
                taskInfo.setCurrentStatus(State.INSTALL);
                if (callback != null) {
                    callback.onInstall(taskInfo);
                }
                mListenerManager.onInstall(taskInfo);
                mDownloadProxy.operateDatebase(taskInfo);
            } else {
                taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson);
                taskInfo.setCurrentStatus(State.INSTALL);
                if (callback != null) {
                    callback.onInstall(taskInfo);
                }
                mListenerManager.onInstall(taskInfo);
                if (taskDBInfo.getCurrentStatus() == null || taskDBInfo.getCurrentStatus() != State.INSTALL) {
                    mDownloadProxy.operateDatebase(taskInfo);
                }
            }
            return null;
        }


        if (taskDBInfo != null) {
            if (taskDBInfo.getCurrentStatus() != null && taskDBInfo.getCurrentStatus() == State.SUCCESS) {
                //TODO 下载成功
                taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson);
                if (callback != null) {
                    callback.onSuccess(taskInfo);
                    if (!TextUtils.isEmpty(taskDBInfo.getPackageName())) {
                        mListenerManager.addSingleTaskCallback(resKey, callback);
                    }
                }
                mListenerManager.onSuccess(taskInfo);
                return null;
            }
        }

        long fileSize = request.fileSize();
        if (fileSize == 0) {
            if (taskDBInfo != null) {
                fileSize = taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize();
            }
        }

        if (currentSize > 0 && currentSize == fileSize) {
            //TODO 下载成功
            if (taskDBInfo != null) {
                taskDBInfo.setCurrentStatus(State.SUCCESS);
                taskDBInfo.setTotalSize(fileSize);
                taskDBInfo.setCurrentSize(currentSize);
                taskDBInfo.setProgress(100);
                taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson);
            }
            taskInfo.setCurrentStatus(State.SUCCESS);
            if (callback != null) {
                callback.onSuccess(taskInfo);
                if (!TextUtils.isEmpty(taskInfo.getPackageName())) {
                    mListenerManager.addSingleTaskCallback(resKey, callback);
                }
            }
            mListenerManager.onSuccess(taskInfo);
            return null;
        }

        if (!hasEnoughDiskSpace(request.fileSize())) {//判断磁盘空间大小
            //TODO 磁盘空间不足
            if (callback != null) {
                callback.onNoEnoughSpace(taskInfo);
            }
            mListenerManager.onNoEnoughSpace(taskInfo);
            return null;
        }

        fileCall = new FileCall(request, mDownloadProxy, taskInfo);
        mFileCalls.put(resKey, fileCall);
        return fileCall;


    }

    private void checkOldFile(FileRequest request, String resKey, TaskInfo taskInfo) {//校验之前下载的文件版本
        int saveVersionCode = -1;
        long saveFileTotalSize = 0;
        if (mHistoryTasks.isEmpty()) {
            TaskDBInfo taskDBInfo = mDBUtil.getTaskDBInfoByResKey(resKey);
            if (taskDBInfo != null) {
                Integer versionCode = taskDBInfo.getVersionCode();//获取数据库中存储的版本号
                saveVersionCode = (versionCode == null ? -1 : versionCode);
                saveFileTotalSize = (taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
                mHistoryTasks.put(taskDBInfo.getResKey(), taskDBInfo);
            }
        } else {
            TaskDBInfo taskDBInfo = mHistoryTasks.get(resKey);
            if (taskDBInfo != null) {
                saveVersionCode = (taskDBInfo.getVersionCode() == null ? -1 : taskDBInfo.getVersionCode());
                saveFileTotalSize = (taskDBInfo.getTotalSize() == null ? 0 : taskDBInfo.getTotalSize());
            }
        }

        if ((request.versionCode() > 0 && (request.versionCode() != saveVersionCode))
                || (request.fileSize() > 0 && (request.fileSize() != saveFileTotalSize))) {
            boolean delete = Utils.deleteDownloadFile(mContext, resKey, taskInfo.getRangeNum());
            if (delete) {
                taskInfo.setProgress(0);
                taskInfo.setCurrentSize(0);
            }
        }
    }

    private boolean hasEnoughDiskSpace(long fileSize) {
        Collection<FileCall> values = mFileCalls.values();
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

    private <T> TaskInfo<T> generateTaskInfo(FileRequest<T> request, File file, Object[] currentSizeAndMultiPositions) {
        TaskInfo taskInfo = new TaskInfo();
        long currentSize = 0;
        if (currentSizeAndMultiPositions != null) {
            currentSize = (long) currentSizeAndMultiPositions[0];
            if (request.byMultiThread()) {
                taskInfo.setStartPositions((long[]) currentSizeAndMultiPositions[1]);
                taskInfo.setEndPositions((long[]) currentSizeAndMultiPositions[2]);
            }
        }

        if (request.fileSize() > 0) {
            taskInfo.setProgress((int) ((currentSize * 100.0f / request.fileSize()) + 0.5f));
        } else {
            TaskDBInfo taskDBInfo = mHistoryTasks.get(request.key());
            if (taskDBInfo != null) {
                taskInfo.setProgress(taskDBInfo.getProgress() == null ? 0 : taskDBInfo.getProgress());
            }
        }

        if (request.byMultiThread()) {
            taskInfo.setRangeNum(multiThreadNum);
        } else {
            taskInfo.setRangeNum(1);
        }


        taskInfo.setFilePath(file.getPath());
        taskInfo.setCurrentSize(currentSize);
        taskInfo.setResKey(request.key());
        taskInfo.setUrl(request.url());
        taskInfo.setTotalSize(request.fileSize());
        taskInfo.setVersionCode(request.versionCode());

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
            taskInfo.setTagJson(mGson.toJson(tag, type));
        }
        taskInfo.setTag(tag);
        taskInfo.setTagType(type);
        return taskInfo;
    }

    private class DownloadListener implements Callback {

        @Override
        public void onNoEnoughSpace(TaskInfo taskInfo) {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onPrepare(taskInfo);
                }
            }
        }

        @Override
        public void onPrepare(TaskInfo taskInfo) {
            mHistoryTasks.put(taskInfo.getResKey(), TaskInfo.taskInfo2TaskDBInfo(taskInfo));
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onPrepare(taskInfo);
                }
            }
        }

        @Override
        public void onFirstFileWrite(TaskInfo taskInfo) {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onFirstFileWrite(taskInfo);
                }
            }
        }

        @Override
        public void onDownloading(TaskInfo taskInfo) {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onDownloading(taskInfo);
                }
            }
        }

        @Override
        public void onWaitingInQueue(TaskInfo taskInfo) {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onWaitingInQueue(taskInfo);
                }
            }
        }

        @Override
        public void onWaitingForWifi(TaskInfo taskInfo) {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onWaitingForWifi(taskInfo);
                }
            }
        }

        @Override
        public void onDelete(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            mHistoryTasks.remove(resKey);
            mFileCalls.remove(resKey);
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onDelete(taskInfo);
                }
            }
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            mHistoryTasks.put(resKey, TaskInfo.taskInfo2TaskDBInfo(taskInfo));
            mFileCalls.remove(resKey);
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onPause(taskInfo);
                }
            }
        }

        @Override
        public void onSuccess(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            mHistoryTasks.put(resKey, TaskInfo.taskInfo2TaskDBInfo(taskInfo));
            mFileCalls.remove(resKey);
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onSuccess(taskInfo);
                }
            }
        }

        @Override
        public void onInstall(TaskInfo taskInfo) {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onInstall(taskInfo);
                }
            }
        }

        @Override
        public void onUnInstall(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            mHistoryTasks.remove(resKey);
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onUnInstall(taskInfo);
                }
            }
        }

        @Override
        public void onFailure(TaskInfo taskInfo) {
            mHistoryTasks.put(taskInfo.getResKey(), TaskInfo.taskInfo2TaskDBInfo(taskInfo));
            mFileCalls.remove(taskInfo.getResKey());
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onFailure(taskInfo);
                }
            }
        }

        @Override
        public void onHaveNoTask() {
            for (Callback listener : mListeners) {
                if (listener != null) {
                    listener.onHaveNoTask();
                }
            }
            haveNoTask = true;
            if (!releaseStarted && !released) {
                startRelease();
            }
        }
    }

    private void startRelease() {
        releaseStarted = true;
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (Utils.isAppExit(mContext)) {
                    SystemClock.sleep(KEEP_ALIVE_TIME_OUTAPP);
                } else {
                    SystemClock.sleep(KEEP_ALIVE_TIME_INAPP);
                }
                if (haveNoTask) {
                    mDownloadProxy.destroy();
                    released = true;
                }
                releaseStarted = false;
            }
        });
    }

    public void onInstall(TaskDBInfo taskDBInfo) {
        TaskInfo taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson);
        taskInfo.setCurrentStatus(State.INSTALL);
        mListenerManager.onInstall(taskInfo);
        if (mFileCalls.isEmpty()) {
            //TODO 没任务了
            mListenerManager.onHaveNoTask();
        }
    }

    public void onUnInstall(TaskDBInfo taskDBInfo) {
        TaskInfo taskInfo = TaskInfo.taskDBInfo2TaskInfo(taskDBInfo, mGson);
        taskInfo.setCurrentStatus(State.UNINSTALL);
        mListenerManager.onUnInstall(taskInfo);
        if (mFileCalls.isEmpty()) {
            //TODO 没任务了
            mListenerManager.onHaveNoTask();
        }
    }

    private static class FileDownloaderBuilder {

        Context context;

        boolean byService;

        int maxSynchronousDownloadNum;

        FileDownloaderBuilder(Context context, boolean byService, int maxSynchronousDownloadNum) {
            this.context = context;
            this.byService = byService;
            this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
        }
    }

    public static class LockConfig {

        boolean isInitProxyFinish;

        boolean isInitHistoryFinish;

        public void setInitProxyFinish(boolean initProxyFinish) {
            isInitProxyFinish = initProxyFinish;
        }

        public void setInitHistoryFinish(boolean initHistoryFinish) {
            isInitHistoryFinish = initHistoryFinish;
        }
    }

    @Override
    public IDownloadProxy.ILocalDownloadProxy produce(boolean byService, int maxSynchronousDownloadNum) {
        IDownloadProxy.ILocalDownloadProxy proxy;
        if (byService) {
            proxy = new ServiceBridge(mContext, mExecutor, maxSynchronousDownloadNum);
        } else {
            proxy = new OkhttpLocalProxy(mContext, maxSynchronousDownloadNum);
        }
        return proxy;
    }
}
