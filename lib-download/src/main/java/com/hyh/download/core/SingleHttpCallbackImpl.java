package com.hyh.download.core;

import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;

import com.hyh.download.FailureCode;
import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.exception.ExceptionHelper;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.RangeUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */
class SingleHttpCallbackImpl extends AbstractHttpCallback {

    private HttpClient client;

    private TaskInfo taskInfo;

    private int oldProgress;

    private HttpCall call;

    private DownloadCallback downloadCallback;

    private IFileChecker fileChecker;

    private IRetryStrategy retryStrategy;

    private volatile boolean cancel;

    private FileWrite mFileWrite;

    private boolean isConnected;

    private long lastPostTimeMillis;

    private boolean isRetryInvalidFileTask;

    private boolean isRetryErrorLengthTask;

    SingleHttpCallbackImpl(Context context,
                           HttpClient client,
                           TaskInfo taskInfo,
                           DownloadCallback downloadCallback,
                           IFileChecker fileChecker) {
        this.client = client;
        this.taskInfo = taskInfo;
        this.oldProgress = RangeUtil.computeProgress(taskInfo.getCurrentSize(), taskInfo.getTotalSize());
        this.downloadCallback = downloadCallback;
        this.fileChecker = fileChecker;
        this.retryStrategy = new RetryStrategyImpl(context, taskInfo.isPermitRetryInMobileData());
    }

    @Override
    public void onResponse(HttpCall call, HttpResponse response) {
        this.call = call;
        if (cancel) {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            return;
        }
        int code = response.code();
        taskInfo.setResponseCode(code);

        if (!response.isSuccessful()) {
            if (!retryDownload(FailureCode.HTTP_ERROR, false)) {
                notifyFailure(FailureCode.HTTP_ERROR);
            }
            return;
        }

        long contentLength = response.contentLength();

        long currentSize = taskInfo.getCurrentSize();
        String filePath = DownloadFileHelper.getTaskFilePath(taskInfo);

        if (!TextUtils.isEmpty(filePath) && currentSize > 0 && !checkIsSupportPartial(response, taskInfo)) {

            DownloadFileHelper.deleteDownloadFile(taskInfo);
            L.d("not support partial content, delete file ");

            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            //不支持断点续传，重新请求下载
            HttpCall newCall = client.newCall(taskInfo.getResKey(), taskInfo.getRequestUrl(), 0);
            newCall.enqueue(this);
            return;
        }

        if (contentLength <= 0) {
            //无法获取到文件长度的下载情况
            taskInfo.setTotalSize(-1L);
        } else {
            long totalSize = taskInfo.getTotalSize();
            if (totalSize <= 0) {
                taskInfo.setTotalSize(response.contentLength() + taskInfo.getCurrentSize());
            }
        }

        handleConnected(response);
        handleDownload(response, taskInfo);
    }

    @Override
    public void onFailure(HttpCall call, Exception e) {
        this.call = call;
        int failureCode = ExceptionHelper.convertFailureCode(e);
        if (!retryDownload(failureCode, false)) {
            notifyFailure(failureCode);
        }
    }

    private boolean checkIsSupportPartial(HttpResponse response, TaskInfo taskInfo) {
        String cacheTargetUrl = taskInfo.getCacheTargetUrl();
        if (!TextUtils.equals(response.url(), cacheTargetUrl)) {
            return false;
        }

        if (response.code() == HttpClient.ResponseCode.PARTIAL_CONTENT) {
            return true;
        }

        final String acceptRanges = response.header(NetworkHelper.ACCEPT_RANGES);

        return "bytes".equals(acceptRanges);
    }


    private void handleConnected(HttpResponse response) {
        isConnected = true;

        DownloadFileHelper.fixTaskFilePath(response, taskInfo);
        taskInfo.setTargetUrl(response.url());
        taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
        taskInfo.setCacheTargetUrl(response.url());

        taskInfo.setContentMD5(response.header(NetworkHelper.CONTENT_MD5));
        taskInfo.setContentType(response.header(NetworkHelper.CONTENT_TYPE));
        taskInfo.setETag(response.header(NetworkHelper.ETAG));
        taskInfo.setLastModified(response.header(NetworkHelper.LAST_MODIFIED));

        notifyConnected(response.headers());
    }

    private void handleDownload(HttpResponse response, TaskInfo taskInfo) {
        final long currentSize = taskInfo.getCurrentSize();
        final long totalSize = taskInfo.getTotalSize();
        mFileWrite = new SingleFileWriteTask(DownloadFileHelper.getTaskFilePath(taskInfo), currentSize, totalSize);
        mFileWrite.write(response, new SingleFileWriteListener(currentSize, totalSize));
    }

    private void notifyConnected(Map<String, List<String>> responseHeaderFields) {
        taskInfo.setCurrentStatus(State.CONNECTED);
        downloadCallback.onConnected(responseHeaderFields);
    }

    private void notifyDownloading(long currentSize, long totalSize) {
        taskInfo.setCurrentStatus(State.DOWNLOADING);
        long elapsedTimeMillis = SystemClock.elapsedRealtime();
        int curProgress = RangeUtil.computeProgress(currentSize, totalSize);
        if (oldProgress != curProgress) {
            oldProgress = curProgress;
            downloadCallback.onDownloading(taskInfo.getCurrentSize());
            lastPostTimeMillis = elapsedTimeMillis;
        } else {
            long diffTimeMillis = elapsedTimeMillis - lastPostTimeMillis;
            if (diffTimeMillis >= 2000) {

                downloadCallback.onDownloading(taskInfo.getCurrentSize());
                lastPostTimeMillis = elapsedTimeMillis;
            }
        }
    }

    private void notifyRetrying(int failureCode, boolean deleteFile) {
        taskInfo.setCurrentStatus(State.RETRYING);
        taskInfo.setFailureCode(failureCode);
        downloadCallback.onRetrying(failureCode, deleteFile);
    }

    private void notifySuccess() {
        taskInfo.setCurrentStatus(State.SUCCESS);
        downloadCallback.onSuccess();
    }

    private void notifyFailure(int failureCode) {
        fixCurrentSize();
        taskInfo.setCurrentStatus(State.FAILURE);
        taskInfo.setFailureCode(failureCode);
        downloadCallback.onFailure(failureCode);
    }

    private class SingleFileWriteListener implements FileWrite.FileWriteListener {

        volatile long currentSize;

        final long totalSize;

        SingleFileWriteListener(long currentSize, long totalSize) {
            this.currentSize = currentSize;
            this.totalSize = totalSize;
        }

        @Override
        public void onWriteFile(long writeLength) {
            retryStrategy.clearCurrentRetryTimes();
            currentSize += writeLength;
            taskInfo.setCurrentSize(currentSize);

            notifyDownloading(currentSize, totalSize);
        }

        @Override
        public void onWriteFinish() {
            if (checkSuccessFile()) {
                notifySuccess();
            } else {
                if (!isRetryInvalidFileTask && taskInfo.isPermitRetryInvalidFileTask()) {
                    isRetryInvalidFileTask = true;
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    if (!retryDownload(FailureCode.FILE_CHECK_FAILURE, true)) {
                        notifyFailure(FailureCode.FILE_CHECK_FAILURE);
                    }
                } else {
                    notifyFailure(FailureCode.FILE_CHECK_FAILURE);
                }
            }
        }

        @Override
        public void onWriteFailure(Exception e) {
            int failureCode = ExceptionHelper.convertFailureCode(e);
            if (!retryDownload(failureCode, false)) {
                notifyFailure(failureCode);
            }
        }

        @Override
        public void onWriteLengthError(long startPosition, long endPosition) {
            L.d("SingleHttpCallbackImpl: onWriteLengthError startPosition = " + startPosition + ", endPosition = " + endPosition);

            if (!isRetryErrorLengthTask) {
                isRetryErrorLengthTask = true;
                DownloadFileHelper.deleteDownloadFile(taskInfo);
                if (!retryDownload(FailureCode.FILE_LENGTH_ERROR, true)) {
                    notifyFailure(FailureCode.FILE_LENGTH_ERROR);
                }
            } else {
                notifyFailure(FailureCode.FILE_LENGTH_ERROR);
            }
        }
    }

    private boolean checkSuccessFile() {
        if (fileChecker == null) {
            return true;
        }
        try {
            return fileChecker.isValidFile(taskInfo.toDownloadInfo());
        } catch (RemoteException e) {
            return true;
        }
    }

    private boolean retryDownload(final int failureCode, final boolean deleteFile) {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        fixCurrentSize();
        if (isConnected) {
            notifyRetrying(failureCode, deleteFile);
        }
        boolean shouldRetry = retryStrategy.shouldRetry();
        if (shouldRetry) {
            HttpCall call = client.newCall(taskInfo.getResKey(), taskInfo.getRequestUrl(), taskInfo.getCurrentSize());
            call.enqueue(this);
        }
        return shouldRetry;
    }

    private void fixCurrentSize() {
        String filePath = DownloadFileHelper.getTaskFilePath(taskInfo);
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            long length = file.length();
            taskInfo.setCurrentSize(length);
        } else {
            taskInfo.setCurrentSize(0);
        }
    }

    @Override
    void cancel() {
        this.cancel = true;
        if (mFileWrite != null) {
            mFileWrite.stop();
        }
        if (this.call != null && !this.call.isCanceled()) {
            this.call.cancel();
        }
        retryStrategy.cancel();
    }
}
