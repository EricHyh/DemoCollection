package com.hyh.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.LocalDownloadProxyImpl;
import com.hyh.download.core.ServiceBridge;
import com.hyh.download.core.TaskListenerManager;
import com.hyh.download.core.TaskStateCache;
import com.hyh.download.utils.DownloadFileHelper;

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

    private final TaskStateCache mTaskStateCache = new TaskStateCache();

    private DownloaderConfig mDownloaderConfig = new DownloaderConfig();

    private final Object mInitProxyLock = new Object();

    private volatile boolean mIsInitProxy;

    private IDownloadProxy mDownloadProxy;

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
        mListenerManager = new TaskListenerManager(mTaskStateCache);
        initProxy();
    }

    private void initProxy() {
        mDownloadProxy.initProxy(new Runnable() {
            @Override
            public void run() {
                synchronized (mInitProxyLock) {
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
            callback.onSuccess(mDownloadProxy.getTaskInfoByKey(key).toDownloadInfo());
            return;
        }

        if (mTaskStateCache.isTaskPrepared(request.key()) || isTaskAlive(request.key())) {
            if (callback != null) {
                mListenerManager.addSingleTaskCallback(request.key(), callback);
            }
            return;
        }

        final TaskInfo taskInfo = getTaskInfo(request);
        mDownloadProxy.insertOrUpdate(taskInfo);
        if (callback != null) {
            mListenerManager.addSingleTaskCallback(request.key(), callback);
        }
        onReceiveStartCommand(taskInfo);
        mDownloadProxy.startTask(taskInfo, request.fileChecker());
    }

    public synchronized void pauseTask(final String resKey) {
        waitingForInitProxyFinish();
        if (mDownloadProxy.isTaskAlive(resKey)) {
            onReceivePauseCommand(resKey);
            mDownloadProxy.pauseTask(resKey);
        }
    }

    public synchronized void deleteTask(final String resKey) {
        waitingForInitProxyFinish();
        onReceiveDeleteCommand(resKey);
        mDownloadProxy.deleteTask(resKey);
    }

    private void onReceiveStartCommand(TaskInfo taskInfo) {
        mListenerManager.onPrepare(taskInfo.toDownloadInfo());
        mDownloadProxy.onReceiveStartCommand(taskInfo.getResKey());
    }

    private void onReceivePauseCommand(String resKey) {
        TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(resKey);
        mListenerManager.onPause(taskInfo.toDownloadInfo());
        mDownloadProxy.onReceivePauseCommand(resKey);
    }

    private void onReceiveDeleteCommand(String resKey) {
        TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(resKey);
        if (taskInfo != null) {
            mListenerManager.onDelete(taskInfo.toDownloadInfo());
        }
        mDownloadProxy.onReceiveDeleteCommand(resKey);
    }

    public boolean isTaskAlive(String resKey) {
        waitingForInitProxyFinish();
        return mDownloadProxy.isTaskAlive(resKey);
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
        if (taskInfo != null) {
            if (!isRequestChanged(request, taskInfo)) {
                fixRequestInfo(request, taskInfo);
            } else {
                DownloadFileHelper.deleteDownloadFile(taskInfo);
                taskInfo = newTaskInfo(request);
            }
        } else {
            taskInfo = newTaskInfo(request);
        }
        taskInfo.setCurrentStatus(State.PREPARE);
        return taskInfo;
    }

    private boolean isRequestChanged(FileRequest request, TaskInfo taskInfo) {
        return request.isForceDownload()
                || isVersionChanged(request, taskInfo)
                || isUrlChanged(request, taskInfo)
                || isFilePathChanged(request, taskInfo);
    }

    private boolean isVersionChanged(FileRequest request, TaskInfo taskInfo) {
        return request.versionCode() != taskInfo.getVersionCode();
    }

    private boolean isUrlChanged(FileRequest request, TaskInfo taskInfo) {
        return request.needVerifyUrl() && !TextUtils.equals(request.url(), taskInfo.getRequestUrl());
    }

    private boolean isFilePathChanged(FileRequest request, TaskInfo taskInfo) {
        String cacheFileDir = taskInfo.getFileDir();
        String cacheFilePath = taskInfo.getFilePath();
        String requestFileDir = request.fileDir();
        String requestFilePath = request.filePath();
        if (!TextUtils.isEmpty(requestFilePath)) {
            if (!TextUtils.equals(cacheFilePath, requestFilePath)) {
                //之前下载额文件路径与现在请求的路径不一致，删除之前下载的文件,表示是一个新的下载
                return true;
            }
        } else if (!TextUtils.isEmpty(requestFileDir)) {
            if (!TextUtils.equals(cacheFileDir, requestFileDir)) {
                //之前下载额文件路径与现在请求的目录不一致，删除之前下载的文件,表示是一个新的下载
                return true;
            }
        }
        return false;
    }

    private void fixRequestInfo(FileRequest request, TaskInfo taskInfo) {
        taskInfo.setRequestUrl(request.url());
        taskInfo.setWifiAutoRetry(request.wifiAutoRetry());
        taskInfo.setPermitMobileDataRetry(request.permitMobileDataRetry());
        taskInfo.setResponseCode(0);
        taskInfo.setTag(request.tag());
    }

    private TaskInfo newTaskInfo(FileRequest request) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setResKey(request.key());
        taskInfo.setRequestUrl(request.url());
        taskInfo.setFileDir(createFileDir(request));
        taskInfo.setFilePath(request.filePath());
        taskInfo.setByMultiThread(request.byMultiThread());
        taskInfo.setWifiAutoRetry(request.wifiAutoRetry());
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
