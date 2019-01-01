package com.hyh.download.core;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.RangeUtil;

import java.io.File;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */
class SingleHttpCallbackImpl extends AbstractHttpCallback {

    private HttpClient client;

    private TaskInfo taskInfo;

    private HttpCall call;

    private DownloadCallback downloadCallback;

    private IRetryStrategy retryStrategy;

    private volatile boolean cancel;

    private FileWrite mFileWrite;

    private long lastPostTimeMillis;

    SingleHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, DownloadCallback downloadCallback) {
        this.client = client;
        this.taskInfo = taskInfo;
        this.downloadCallback = downloadCallback;
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
            if (!retryDownload()) {
                notifyFailure();
            }
            return;
        }

        long contentLength = response.contentLength();

        long currentSize = taskInfo.getCurrentSize();
        String filePath = taskInfo.getFilePath();
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

        if (contentLength > 0
                && (code == Constants.ResponseCode.OK || code == Constants.ResponseCode.PARTIAL_CONTENT)) {//请求数据成功
            long totalSize = taskInfo.getTotalSize();
            if (totalSize <= 0) {
                taskInfo.setTotalSize(response.contentLength() + taskInfo.getCurrentSize());
            }
            handleDownload(response, taskInfo);
        } else if (contentLength <= 0 && (code == Constants.ResponseCode.OK || code == Constants.ResponseCode.PARTIAL_CONTENT)) {
            //无法获取到文件长度的下载情况
            taskInfo.setTotalSize(-1L);
            handleDownload(response, taskInfo);
        } else if (code == Constants.ResponseCode.NOT_FOUND) {
            //未找到文件
            notifyFailure();
        } else {
            if (!retryDownload()) {
                notifyFailure();
            }
        }
    }

    @Override
    public void onFailure(HttpCall call, Exception e) {
        this.call = call;
        if (!retryDownload()) {
            notifyFailure();
        }
    }

    private boolean checkIsSupportPartial(HttpResponse response, TaskInfo taskInfo) {
        String cacheTargetUrl = taskInfo.getCacheTargetUrl();
        if (!TextUtils.equals(response.url(), cacheTargetUrl)) {
            return false;
        }

        if (response.code() == Constants.ResponseCode.PARTIAL_CONTENT) {
            return true;
        }

        final String acceptRanges = response.header(NetworkHelper.ACCEPT_RANGES);

        return "bytes".equals(acceptRanges);
    }

    private void handleDownload(HttpResponse response, TaskInfo taskInfo) {
        taskInfo.setTargetUrl(response.url());
        taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
        taskInfo.setCacheTargetUrl(response.url());
        String filePath = DownloadFileHelper.fixFilePath(response, taskInfo);

        downloadCallback.onConnected(taskInfo, response.headers());

        final long currentSize = taskInfo.getCurrentSize();
        final long totalSize = taskInfo.getTotalSize();
        mFileWrite = new SingleFileWriteTask(filePath, currentSize, totalSize);
        mFileWrite.write(response, new SingleFileWriteListener(currentSize, totalSize));
    }

    private void notifyDownloading(long currentSize, long totalSize) {
        long elapsedTimeMillis = SystemClock.elapsedRealtime();
        int oldProgress = taskInfo.getProgress();
        int curProgress = RangeUtil.computeProgress(currentSize, totalSize);
        if (oldProgress != curProgress) {
            taskInfo.setProgress(RangeUtil.computeProgress(currentSize, totalSize));
            downloadCallback.onDownloading(taskInfo);
            lastPostTimeMillis = elapsedTimeMillis;
        } else {
            long diffTimeMillis = elapsedTimeMillis - lastPostTimeMillis;
            if (diffTimeMillis >= 2000) {
                downloadCallback.onDownloading(taskInfo);
                lastPostTimeMillis = elapsedTimeMillis;
            }
        }
    }

    private void notifySuccess() {
        taskInfo.setProgress(100);
        downloadCallback.onSuccess(taskInfo);
    }

    private void notifyFailure() {
        fixCurrentSize();
        downloadCallback.onFailure(taskInfo);
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
            notifySuccess();
        }

        @Override
        public void onWriteFailure() {
            if (!retryDownload()) {
                notifyFailure();
            }
        }

        @Override
        public void onWriteLengthError(long startPosition, long endPosition) {

        }
    }

    private boolean retryDownload() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        boolean shouldRetry = retryStrategy.shouldRetry(new IRetryStrategy.onWaitingListener() {
            @Override
            public void onWaiting() {
                notifyDownloading(taskInfo.getCurrentSize(), taskInfo.getTotalSize());
            }
        });
        if (shouldRetry) {
            fixCurrentSize();
            HttpCall call = client.newCall(taskInfo.getResKey(), taskInfo.getRequestUrl(), taskInfo.getCurrentSize());
            call.enqueue(this);
        }
        return shouldRetry;
    }

    private void fixCurrentSize() {
        String filePath = taskInfo.getFilePath();
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            long length = file.length();
            taskInfo.setCurrentSize(length);
            taskInfo.setProgress(RangeUtil.computeProgress(length, taskInfo.getTotalSize()));
        } else {
            taskInfo.setCurrentSize(0);
            taskInfo.setProgress(0);
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
