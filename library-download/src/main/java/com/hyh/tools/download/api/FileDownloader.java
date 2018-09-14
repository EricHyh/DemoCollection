package com.hyh.tools.download.api;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.hyh.tools.download.bean.Command;
import com.hyh.tools.download.bean.State;
import com.hyh.tools.download.bean.TagInfo;
import com.hyh.tools.download.bean.TaskInfo;
import com.hyh.tools.download.db.bean.TaskDBInfo;
import com.hyh.tools.download.internal.DownloadProxyFactory;
import com.hyh.tools.download.internal.IDownloadProxy;
import com.hyh.tools.download.internal.LocalDownloadProxyImpl;
import com.hyh.tools.download.internal.ServiceBridge;
import com.hyh.tools.download.internal.TaskListenerManager;
import com.hyh.tools.download.paser.TagParser;
import com.hyh.tools.download.paser.TagParserFactory;
import com.hyh.tools.download.utils.FD_DBUtil;
import com.hyh.tools.download.utils.FD_FileUtil;
import com.hyh.tools.download.utils.FD_MultiUtil;
import com.hyh.tools.download.utils.FD_PackageUtil;
import com.hyh.tools.download.utils.FD_Utils;

import java.io.File;
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

    private ThreadPoolExecutor mExecutor = FD_Utils.buildExecutor(4, 4, 60, "FileDownload Thread", true);

    private TagParser mTagParser;

    private final LockConfig mLockConfig = new LockConfig();

    private Context mContext;

    private IDownloadProxy.ILocalDownloadProxy mDownloadProxy;//本地下载代理类

    private ConcurrentHashMap<String, FileCall> mFileCalls = new ConcurrentHashMap<>();//存储当前正在进行的任务

    private FD_DBUtil mFD_DBUtil;//数据库操作工具类

    private volatile boolean haveNoTask = true;//是否有下载任务正在进行

    private volatile boolean released = false;//内存是否已释放

    private volatile boolean releaseStarted;//是否开始释放内存的任务

    private List<Callback> mListeners = new ArrayList<>();//存储外部注册的回调

    private Map<String, TaskDBInfo> mHistoryTasks = new ConcurrentHashMap<>();//数据库中所有任务的列表

    private final TaskListenerManager mListenerManager;


    public static FileDownloaderBuilder startInit(Context context) {
        return new FileDownloaderBuilder(context);
    }

    private static void initFileDownloader(FileDownloaderBuilder builder) {
        if (sFileDownloader != null) {
            return;
        }
        synchronized (FileDownloader.class) {
            if (sFileDownloader == null) {
                sFileDownloader = new FileDownloader(builder);
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
        this.multiThreadNum = FD_MultiUtil.computeMultiThreadNum(builder.maxSynchronousDownloadNum);
        this.mFD_DBUtil = FD_DBUtil.getInstance(this.mContext);
        this.mListenerManager = new TaskListenerManager(new DownloadListener());
        this.mDownloadProxy = produce(builder.byService, builder.isIndependentProcess, builder.maxSynchronousDownloadNum);
        this.mTagParser = TagParserFactory.produce(builder.mTagParser);

        mDownloadProxy.setAllTaskCallback(mListenerManager);

        mDownloadProxy.initProxy(mLockConfig);

        initHistoryTasks();
    }

    private void initHistoryTasks() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                waitInitProxyFinish();
                mHistoryTasks = mFD_DBUtil.getAllTaskMap();
                synchronized (mLockConfig) {
                    mLockConfig.setInitHistoryFinish(true);
                    mLockConfig.notifyAll();
                }
            }
        });
    }

    public void startTask(FileRequest fileRequest) {
        startTask(fileRequest, null);
    }

    public void startTask(final FileRequest fileRequest, final Callback callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FileCall fileCall = newCall(fileRequest, callback);
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

    public void startWaitingForWifiTasks() {
        getSaveInDBWaitingForWifiTasksAsynch(new SearchListener<List<TaskInfo>>() {
            @Override
            public void onResult(List<TaskInfo> result) {
                if (result != null && !result.isEmpty()) {
                    for (TaskInfo taskInfo : result) {
                        FileRequest fileRequest = new FileRequest.Builder()
                                .byMultiThread(taskInfo.getRangeNum() > 1)
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
        TaskDBInfo taskDBInfo = mFD_DBUtil.getTaskDBInfoByResKey(resKey);
        return taskDBInfo != null && taskDBInfo.getCurrentStatus() == State.SUCCESS;
    }

    public PendingIntent buildPendingIntent(FileRequest fileRequest, Callback callback) {
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
                    TaskDBInfo taskDBInfo = mFD_DBUtil.getTaskDBInfoByResKey(resKey);
                    if (taskDBInfo != null) {
                        mListenerManager.onDelete(FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser));
                        FD_FileUtil.deleteDownloadFile(mContext, resKey, taskDBInfo.getRangeNum() == null ?
                                0 : taskDBInfo.getRangeNum());
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

    public <T> void addListener(Callback callback) {
        mListeners.add(callback);
    }

    public <T> void removeListener(Callback callback) {
        mListeners.remove(callback);
    }

    public <T> void removeAllListener() {
        mListeners.clear();
    }

    public List<TaskInfo> getSaveInDBWaitingForWifiTasksSynch() {
        ArrayList<TaskInfo> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mFD_DBUtil.getWaitingForWifiTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public List<TaskInfo> getSaveInDBSuccessTasksSynch() {
        ArrayList<TaskInfo> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mFD_DBUtil.getSuccessTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public List<TaskInfo> getSaveInDBInstalledTasksSynch() {
        ArrayList<TaskInfo> taskInfos = new ArrayList<>();
        List<TaskDBInfo> list = mFD_DBUtil.getInstalledTasks();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
            taskInfos.add(taskInfo);
        }
        return taskInfos;
    }

    public List<TaskInfo> getAllAppsSynch() {
        waitInitProxyFinish();
        List<TaskDBInfo> list = mFD_DBUtil.getAllTaskList();
        List<TaskInfo> taskInfos = new ArrayList<>();
        for (TaskDBInfo taskDBInfo : list) {
            TaskInfo taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
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

    public void getSaveInDBWaitingForWifiTasksAsynch(final SearchListener<List<TaskInfo>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo> taskInfos = getSaveInDBWaitingForWifiTasksSynch();
                callback.onResult(taskInfos);
            }
        });
    }

    public void getSaveInDBSuccessTasksAsynch(final SearchListener<List<TaskInfo>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo> taskInfos = getSaveInDBSuccessTasksSynch();
                callback.onResult(taskInfos);
            }
        });

    }

    public void getSaveInDBInstalledTasksAsynch(final SearchListener<List<TaskInfo>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo> taskInfos = getSaveInDBInstalledTasksSynch();
                callback.onResult(taskInfos);
            }
        });

    }

    public void getAllAppsAsynch(final SearchListener<List<TaskInfo>> callback) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo> taskInfos = getAllAppsSynch();
                callback.onResult(taskInfos);
            }
        });
    }

    private FileCall newCall(final FileRequest request, Callback callback) {
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

        // TODO: 2017/6/23 问下其他进程的兄弟有没有在下载这个任务
        boolean isFileDownloading = mDownloadProxy.isOtherProcessDownloading(resKey);
        if (isFileDownloading) {
            return null;
        }

        haveNoTask = false;

        long startTime = System.currentTimeMillis();

        File file = FD_FileUtil.getDownLoadFile(mContext, request.key());//获取已下载文件
        Object[] currentSizeAndMultiPositions = null;
        if (file.exists()) {
            currentSizeAndMultiPositions = FD_MultiUtil.getCurrentSizeAndMultiPositions(mContext, request,
                    file, mHistoryTasks.get(resKey));
        }

        long endTime = System.currentTimeMillis();

        Log.d("FDL_HH", "newCall: getCurrentSizeAndMultiPositions time=" + (endTime - startTime) / 1000);

        TaskInfo taskInfo = generateTaskInfo(request, file, currentSizeAndMultiPositions);


        //校验之前下载的文件版本
        checkOldFile(request, resKey, taskInfo);

        long currentSize = taskInfo.getCurrentSize();

        TaskDBInfo taskDBInfo = mHistoryTasks.isEmpty() ? mFD_DBUtil.getTaskDBInfoByResKey(resKey) : mHistoryTasks.get(resKey);

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
                oldVersionCode = FD_PackageUtil.getVersionCode(mContext, request.packageName());
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
                taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
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
                taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
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
                taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
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
            TaskDBInfo taskDBInfo = mFD_DBUtil.getTaskDBInfoByResKey(resKey);
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
            boolean delete = FD_FileUtil.deleteDownloadFile(mContext, resKey, taskInfo.getRangeNum());
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
        if (FD_FileUtil.externalMemoryAvailable()) {
            long availableSize = FD_FileUtil.getAvailableExternalMemorySize();
            if (availableSize > fileSize) {
                return true;
            }
        } else {
            long availableSize = FD_FileUtil.getAvailableInternalMemorySize();
            if (availableSize > fileSize) {
                return true;
            }
        }
        return false;
    }

    private TaskInfo generateTaskInfo(FileRequest request, File file, Object[] currentSizeAndMultiPositions) {
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
        Object tag = request.tag();
        String tagClassName = request.tagClassName();
        String tagStr = mTagParser.toString(tag);
        taskInfo.setTagInfo(new TagInfo(tagStr, tagClassName, tag));
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
            mHistoryTasks.put(taskInfo.getResKey(), FD_Utils.taskInfo2TaskDBInfo(taskInfo));
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
            mHistoryTasks.put(resKey, FD_Utils.taskInfo2TaskDBInfo(taskInfo));
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
            mHistoryTasks.put(resKey, FD_Utils.taskInfo2TaskDBInfo(taskInfo));
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
            mHistoryTasks.put(taskInfo.getResKey(), FD_Utils.taskInfo2TaskDBInfo(taskInfo));
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
                if (FD_PackageUtil.isAppExit(mContext)) {
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
        TaskInfo taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
        taskInfo.setCurrentStatus(State.INSTALL);
        mListenerManager.onInstall(taskInfo);
        if (mFileCalls.isEmpty()) {
            //TODO 没任务了
            mListenerManager.onHaveNoTask();
        }
    }

    public void onUnInstall(TaskDBInfo taskDBInfo) {
        TaskInfo taskInfo = FD_Utils.taskDBInfo2TaskInfo(taskDBInfo, mTagParser);
        taskInfo.setCurrentStatus(State.UNINSTALL);
        mListenerManager.onUnInstall(taskInfo);
        if (mFileCalls.isEmpty()) {
            //TODO 没任务了
            mListenerManager.onHaveNoTask();
        }
    }

    public static class FileDownloaderBuilder {

        Context context;

        boolean byService = false;

        boolean isIndependentProcess = false;

        int maxSynchronousDownloadNum = 2;

        TagParser mTagParser;


        private FileDownloaderBuilder(Context context) {
            this.context = context;
        }


        FileDownloaderBuilder(Context context, boolean byService, int maxSynchronousDownloadNum, TagParser tagParser) {
            this.context = context;
            this.byService = byService;
            this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
            this.mTagParser = tagParser;
        }

        public void byService(boolean byService) {
            this.byService = byService;
        }

        public void isIndependentProcess(boolean isIndependentProcess) {
            this.isIndependentProcess = isIndependentProcess;
        }

        public void maxSynchronousDownloadNum(int maxSynchronousDownloadNum) {
            this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
        }

        public void jsonParser(TagParser tagParser) {
            this.mTagParser = tagParser;
        }

        public void init() {
            FileDownloader.initFileDownloader(this);
        }
    }

    public static class LockConfig {

        volatile boolean isInitProxyFinish;

        volatile boolean isInitHistoryFinish;

        public void setInitProxyFinish(boolean initProxyFinish) {
            isInitProxyFinish = initProxyFinish;
        }

        public void setInitHistoryFinish(boolean initHistoryFinish) {
            isInitHistoryFinish = initHistoryFinish;
        }
    }

    @Override
    public IDownloadProxy.ILocalDownloadProxy produce(boolean byService, boolean isIndependentProcess, int maxSynchronousDownloadNum) {
        IDownloadProxy.ILocalDownloadProxy proxy;
        if (byService) {
            proxy = new ServiceBridge(mContext, isIndependentProcess, mExecutor, maxSynchronousDownloadNum);
        } else {
            proxy = new LocalDownloadProxyImpl(mContext, mExecutor, maxSynchronousDownloadNum);
        }
        return proxy;
    }
}
