package com.hyh.download;

import android.annotation.SuppressLint;
import android.content.Context;

import com.hyh.download.core.DownloadProxyConfig;
import com.hyh.download.core.DownloadProxyImpl;
import com.hyh.download.core.IDownloadProxy;
import com.hyh.download.core.RequestInfo;
import com.hyh.download.core.ServiceBridge;
import com.hyh.download.core.TaskListenerManager;
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
                listener.onSuccess(mDownloadProxy.getDownloadInfoByKey(key));
            }
            return;
        }
        //final TaskInfo taskInfo = getTaskInfo(request);

        if (listener != null) {
            mListenerManager.addSingleTaskListener(request.key(), listener);
        }
        mDownloadProxy.startTask(RequestInfo.create(request), request.fileChecker());
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
        DownloadInfo downloadInfo = mDownloadProxy.getDownloadInfoByKey(resKey);
        if (downloadInfo != null) {
            return DownloadFileHelper.getTaskFilePath(downloadInfo);
        }
        return null;
    }

    public synchronized DownloadInfo getDownloadInfo(String resKey) {
        waitingForInitProxyFinish();
        return mDownloadProxy.getDownloadInfoByKey(resKey);
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
}
