package com.hyh.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.hyh.download.core.DownloadProxyConfig;
import com.hyh.download.core.DownloadProxyImpl;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.ServiceBridge;
import com.hyh.download.core.TaskListenerManager;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;

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

    private DownloaderConfig mDownloaderConfig;

    private final Object mInitProxyLock = new Object();

    private final TaskListenerManager mListenerManager = new TaskListenerManager();

    private volatile boolean mIsInitProxy;

    private IDownloadProxy mDownloadProxy;

    private FileDownloader() {
    }

    public void init(Context context) {
        init(context, null);
    }

    public void init(Context context, DownloaderConfig downloaderConfig) {
        if (mContext != null) {
            return;
        }
        mContext = context.getApplicationContext();
        if (downloaderConfig == null) {
            mDownloaderConfig = new DownloaderConfig.Builder().build();
        } else {
            mDownloaderConfig = downloaderConfig;
        }

        mDownloadProxy = createDownloadProxy();

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

    public synchronized void startTask(String url) {
        startTask(new FileRequest.Builder().url(url).byMultiThread(true).build(), null);
    }

    public synchronized void startTask(FileRequest request) {
        startTask(request, null);
    }

    public synchronized void startTask(final FileRequest request, final TaskListener listener) {
        waitingForInitProxyFinish();
        String key = request.key();

        if (isTaskAlive(request.key())) {
            if (listener != null) {
                mListenerManager.addSingleTaskListener(request.key(), listener);
            }
            return;
        }


        if (!request.forceDownload() && isFileDownloaded(key, request.fileChecker())) {
            if (listener != null) {
                listener.onSuccess(mDownloadProxy.getTaskInfoByKey(key).toDownloadInfo());
            }
            return;
        }

        final TaskInfo taskInfo = getTaskInfo(request);
        if (listener != null) {
            mListenerManager.addSingleTaskListener(request.key(), listener);
        }
        mDownloadProxy.startTask(taskInfo, request.fileChecker());
    }

    public synchronized void pauseTask(final String resKey) {
        waitingForInitProxyFinish();
        if (mDownloadProxy.isTaskAlive(resKey)) {
            mDownloadProxy.pauseTask(resKey);
        }
    }

    public synchronized void deleteTask(final String resKey) {
        waitingForInitProxyFinish();
        mDownloadProxy.deleteTask(resKey);
    }

    public synchronized boolean isTaskAlive(String resKey) {
        waitingForInitProxyFinish();
        return mDownloadProxy.isTaskAlive(resKey);
    }

    public synchronized boolean isFileDownloaded(String resKey) {
        waitingForInitProxyFinish();
        return mDownloadProxy.isFileDownloaded(resKey, null);
    }

    public synchronized boolean isFileDownloaded(String resKey, FileChecker fileChecker) {
        waitingForInitProxyFinish();
        return mDownloadProxy.isFileDownloaded(resKey, fileChecker);
    }

    public synchronized String getFilePath(String resKey) {
        waitingForInitProxyFinish();
        TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(resKey);
        if (taskInfo != null) {
            return DownloadFileHelper.getTaskFilePath(taskInfo);
        }
        return null;
    }

    public synchronized DownloadInfo getDownloadInfo(String resKey) {
        waitingForInitProxyFinish();
        TaskInfo taskInfo = mDownloadProxy.getTaskInfoByKey(resKey);
        if (taskInfo == null) {
            return null;
        }
        return taskInfo.toDownloadInfo();
    }

    public void addDownloadListener(String resKey, TaskListener listener) {
        mListenerManager.addSingleTaskListener(resKey, listener);
    }

    public void removeDownloadListener(String resKey, TaskListener listener) {
        mListenerManager.removeSingleTaskListener(resKey, listener);
    }

    public void removeDownloadListeners(String resKey) {
        mListenerManager.removeSingleTaskCallbacks(resKey);
    }

    private IDownloadProxy createDownloadProxy() {
        IDownloadProxy proxy;
        if (mDownloaderConfig.isByService()) {
            proxy = new ServiceBridge(mContext,
                    mDownloaderConfig,
                    mListenerManager);
        } else {
            proxy = new DownloadProxyImpl(mContext,
                    DownloadProxyConfig.create(mDownloaderConfig),
                    mListenerManager,
                    mDownloaderConfig.getGlobalFileChecker());
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
                if (isFileDownloaded(request.key(), request.fileChecker())) {
                    taskInfo = newTaskInfo(request);
                } else {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    taskInfo = newTaskInfo(request);
                }
            }
        } else {
            taskInfo = newTaskInfo(request);
        }
        taskInfo.setCurrentStatus(State.NONE);
        return taskInfo;
    }

    private boolean isRequestChanged(FileRequest request, TaskInfo taskInfo) {
        return request.forceDownload()
                || isUrlChanged(request, taskInfo)
                || isFilePathChanged(request, taskInfo);
    }

    private boolean isUrlChanged(FileRequest request, TaskInfo taskInfo) {
        return request.needVerifyUrl() && !TextUtils.equals(request.url(), taskInfo.getRequestUrl());
    }

    private boolean isFilePathChanged(FileRequest request, TaskInfo taskInfo) {
        String requestFileDir = request.fileDir();
        String requestFileName = request.fileName();
        String cacheFileDir = taskInfo.getFileDir();
        String cacheFileName = taskInfo.getFileName();
        if (!TextUtils.isEmpty(requestFileDir) && !TextUtils.equals(cacheFileDir, requestFileDir)) {
            L.d("requestFileDir:" + requestFileDir + ", cacheFileDir:" + cacheFileDir);
            return true;
        }
        if (!TextUtils.isEmpty(requestFileName) && !TextUtils.equals(cacheFileDir, cacheFileName)) {
            L.d("requestFileName:" + requestFileName + ", cacheFileName:" + cacheFileName);
            return true;
        }
        return false;
    }

    private void fixRequestInfo(FileRequest request, TaskInfo taskInfo) {
        taskInfo.setRequestUrl(request.url());
        taskInfo.setTargetUrl(null);

        taskInfo.setWifiAutoRetry(request.wifiAutoRetry());
        taskInfo.setPermitRetryInMobileData(request.permitRetryInMobileData());
        taskInfo.setPermitRetryInvalidFileTask(request.permitRetryInvalidFileTask());
        taskInfo.setPermitRecoverTask(request.permitRecoverTask());

        taskInfo.setResponseCode(0);
        taskInfo.setFailureCode(0);
        taskInfo.setTag(request.tag());
    }

    private TaskInfo newTaskInfo(FileRequest request) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setResKey(request.key());
        taskInfo.setRequestUrl(request.url());

        String fileDir = createFileDir(request);
        taskInfo.setFileDir(fileDir);
        String fileName = request.fileName();

        fileName = DownloadFileHelper.fixFileExists(fileDir, fileName);

        taskInfo.setFileName(fileName);
        taskInfo.setByMultiThread(request.byMultiThread());

        taskInfo.setWifiAutoRetry(request.wifiAutoRetry());
        taskInfo.setPermitRetryInMobileData(request.permitRetryInMobileData());
        taskInfo.setPermitRetryInvalidFileTask(request.permitRetryInvalidFileTask());
        taskInfo.setPermitRecoverTask(request.permitRecoverTask());

        taskInfo.setTag(request.tag());
        return taskInfo;
    }

    private String createFileDir(FileRequest fileRequest) {
        String fileDir = fileRequest.fileDir();
        if (TextUtils.isEmpty(fileDir)) {
            fileDir = mDownloaderConfig.getDefaultFileDir();
        }
        if (TextUtils.isEmpty(fileDir)) {
            fileDir = DownloadFileHelper.getDefaultFileDir(mContext);
        }
        DownloadFileHelper.ensureCreated(fileDir);
        return fileDir;
    }
}
