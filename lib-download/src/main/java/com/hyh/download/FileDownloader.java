package com.hyh.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.hyh.download.bean.TaskInfo;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.LocalDownloadProxyImpl;
import com.hyh.download.core.ServiceBridge;
import com.hyh.download.core.TaskListenerManager;
import com.hyh.download.utils.DownloadFileHelper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2018/12/12
 */

public class FileDownloader {

    @SuppressLint("StaticFieldLeak")
    private static FileDownloader sFileDownloader = new FileDownloader();

    public static FileDownloader getInstance() {
        return sFileDownloader;
    }

    private Context mContext;

    private DownloaderConfig mDownloaderConfig = new DownloaderConfig();

    private final Object mInitProxyLock = new Object();

    private volatile boolean mIsInitProxy;

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

    private IDownloadProxy mDownloadProxy;//本地下载代理类

    private TaskListenerManager mListenerManager;

    private FileDownloader() {
    }

    public void init(Context context, DownloaderConfig downloaderConfig) {
        if (mContext != null) {
            return;
        }
        mContext = context.getApplicationContext();
        mDownloaderConfig = downloaderConfig;
        mDownloadProxy = createDownloadProxy();
        mListenerManager = new TaskListenerManager();
        initProxy();
    }

    private void initProxy() {
        mExecutor.execute(new Runnable() {

            @Override
            public void run() {
                synchronized (mInitProxyLock) {
                    mDownloadProxy.initProxy();
                    mIsInitProxy = true;
                    mInitProxyLock.notifyAll();
                }
            }
        });
    }

    private void waitingForInitProxyFinish() {
        if (!mIsInitProxy) {
            synchronized (mInitProxyLock) {
                if (!mIsInitProxy) {
                    while (true) {
                        try {
                            mInitProxyLock.wait();
                        } catch (Exception e) {
                            //
                        }
                        if (mIsInitProxy) {
                            return;
                        }
                    }
                }
            }
        }
    }

    public synchronized void startTask(final FileRequest request, final Callback callback) {
        waitingForInitProxyFinish();
        String key = request.key();
        if (isFileDownloaded(key)) {
            callback.onSuccess(mDownloadProxy.getTaskInfoByKey(key));
            return;
        }
        if (isFileDownloading(request.key())) {
            if (callback != null) {
                mListenerManager.addSingleTaskCallback(request.key(), callback);
            }
            return;
        }

        final TaskInfo taskInfo = getTaskInfo(request);
        mDownloadProxy.operateDatabase(taskInfo);
        if (callback != null) {
            mListenerManager.addSingleTaskCallback(request.key(), callback);
        }
        onReceiveStartCommand(taskInfo);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDownloadProxy.enqueue(Command.START, taskInfo);
            }
        });
    }

    public synchronized void pauseTask(final String resKey) {
        waitingForInitProxyFinish();
        if (mDownloadProxy.isFileDownloading(resKey)) {
            final TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(resKey);
            onReceivePauseCommand(taskInfo);
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mDownloadProxy.enqueue(Command.PAUSE, taskInfo);
                }
            });
        }
    }

    public synchronized void deleteTask(final String resKey) {
        waitingForInitProxyFinish();
        if (mDownloadProxy.isFileDownloading(resKey)) {
            final TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(resKey);
            onReceiveDeleteCommand(taskInfo);
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    mDownloadProxy.enqueue(Command.DELETE, taskInfo);
                }
            });
        } else {
            mDownloadProxy.deleteTask(resKey);
        }
    }

    private void onReceiveStartCommand(TaskInfo taskInfo) {
        mListenerManager.onPrepare(taskInfo);
        mDownloadProxy.onReceiveStartCommand(taskInfo);
        mListenerManager.onPrepare(taskInfo);
    }

    private void onReceivePauseCommand(TaskInfo taskInfo) {
        mListenerManager.onPause(taskInfo);
        mDownloadProxy.onReceivePauseCommand(taskInfo);
        mListenerManager.onPause(taskInfo);
    }

    private void onReceiveDeleteCommand(TaskInfo taskInfo) {
        mListenerManager.onDelete(taskInfo);
        mDownloadProxy.onReceiveDeleteCommand(taskInfo);
        mListenerManager.onDelete(taskInfo);
    }

    public boolean isFileDownloading(String resKey) {
        waitingForInitProxyFinish();
        return mDownloadProxy.isFileDownloading(resKey);
    }

    public boolean isFileDownloaded(String resKey) {
        waitingForInitProxyFinish();
        return mDownloadProxy.isFileDownloaded(resKey);
    }

    private IDownloadProxy createDownloadProxy() {
        IDownloadProxy proxy;
        if (mDownloaderConfig.isByService()) {
            proxy = new ServiceBridge(mContext,
                    mDownloaderConfig.isIndependentProcess(),
                    mDownloaderConfig.getMaxSyncDownloadNum(),
                    mListenerManager);
        } else {
            proxy = new LocalDownloadProxyImpl(mContext,
                    mDownloaderConfig.getMaxSyncDownloadNum(),
                    mListenerManager);
        }
        return proxy;
    }

    private TaskInfo getTaskInfo(FileRequest request) {
        String key = request.key();
        TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(key);
        if (taskInfo != null
                && !request.isForceDownload()
                && !isVersionChanged(request, taskInfo)
                && !isUrlChanged(request, taskInfo)) {
            fixDownloadUrl(request, taskInfo);
            fixFilePath(request, taskInfo);
            fixByMultiThread(request, taskInfo);
        } else {
            taskInfo = newTaskInfo(request);
        }
        taskInfo.setCurrentStatus(State.PREPARE);
        return taskInfo;
    }

    private boolean isVersionChanged(FileRequest request, TaskInfo taskInfo) {
        return request.versionCode() != taskInfo.getVersionCode();
    }

    private boolean isUrlChanged(FileRequest request, TaskInfo taskInfo) {
        return request.needVerifyUrl() && !TextUtils.equals(request.url(), taskInfo.getRequestUrl());
    }

    private void fixDownloadUrl(FileRequest request, TaskInfo taskInfo) {

    }

    private void fixFilePath(FileRequest request, TaskInfo taskInfo) {
        String cacheFileDir = taskInfo.getFileDir();
        String cacheFilePath = taskInfo.getFilePath();
        String requestFileDir = request.fileDir();
        String requestFilePath = request.filePath();
        if (!TextUtils.isEmpty(requestFilePath)) {
            if (!TextUtils.equals(cacheFilePath, requestFilePath)) {
                //之前下载额文件路径与现在请求的路径不一致，删除之前下载的文件,表示是一个新的下载
                DownloadFileHelper.deleteFile(cacheFilePath);
                taskInfo.setFileDir(DownloadFileHelper.getParenFilePath(requestFilePath));
                taskInfo.setFilePath(requestFilePath);
                taskInfo.setCurrentSize(0);
                taskInfo.setTotalSize(0);
            }
        } else if (!TextUtils.isEmpty(requestFileDir)) {
            if (!TextUtils.equals(cacheFileDir, requestFileDir)) {
                //之前下载额文件路径与现在请求的目录不一致，删除之前下载的文件,表示是一个新的下载
                DownloadFileHelper.deleteFile(cacheFilePath);
                taskInfo.setFileDir(requestFileDir);
                taskInfo.setCurrentSize(0);
                taskInfo.setTotalSize(0);
            }
        }
    }

    private void fixByMultiThread(FileRequest request, TaskInfo taskInfo) {
        String filePath = taskInfo.getFilePath();
        if (DownloadFileHelper.getFileLength(filePath) <= 0) {
            taskInfo.setByMultiThread(request.byMultiThread());
        }
    }

    private TaskInfo newTaskInfo(FileRequest request) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setResKey(request.key());
        taskInfo.setRequestUrl(request.url());
        taskInfo.setFileDir(createFileDir(request));
        taskInfo.setFilePath(request.filePath());
        taskInfo.setByMultiThread(request.byMultiThread());
        taskInfo.setWifiAutoRetryFailedTask(request.wifiAutoRetryFailedTask());
        taskInfo.setPermitMobileDataRetry(request.permitMobileDataRetry());
        taskInfo.setTag(request.tag());
        return taskInfo;
    }

    private String createFileDir(FileRequest fileRequest) {
        String fileDir;
        String filePath = fileRequest.filePath();
        if (!TextUtils.isEmpty(filePath)) {
            fileDir = DownloadFileHelper.getParenFilePath(filePath);
        } else if (!TextUtils.isEmpty(fileRequest.fileDir())) {
            fileDir = fileRequest.fileDir();
        } else if (!TextUtils.isEmpty(mDownloaderConfig.getDefaultFileDir())) {
            fileDir = mDownloaderConfig.getDefaultFileDir();
        } else {
            fileDir = DownloadFileHelper.getDefaultFileDir(mContext);
        }
        DownloadFileHelper.ensureCreated(fileDir);
        return fileDir;
    }
}
