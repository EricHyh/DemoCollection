package com.hyh.download.core;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.ProgressHelper;

import java.io.File;
import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */
class SingleHttpCallbackImpl extends AbstractHttpCallback {

    private Context context;

    private HttpClient client;

    private TaskInfo taskInfo;

    private HttpCall call;

    private DownloadCallback downloadCallback;

    private volatile boolean cancel;

    //总重试的次数
    private int totalRetryTimes = 0;
    //重试的当前次数
    private volatile int currentRetryTimes = 0;
    //重试的最大次数
    private static final int RETRY_MAX_TIMES = 3;
    //每次重试的延时
    private static final long RETRY_DELAY = 1000 * 2;
    //获取wifi重试的最大次数
    private static final int SEARCH_WIFI_MAX_TIMES = 15;
    //总重试的最大次数
    private static final int TOTAL_RETRY_MAX_TIMES = 20;

    private FileWrite mFileWrite;

    SingleHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, DownloadCallback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.downloadCallback = downloadCallback;
    }

    @Override
    public void onResponse(HttpCall call, HttpResponse response) throws IOException {
        this.call = call;
        if (cancel) {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            return;
        }
        int code = response.code();
        taskInfo.setResponseCode(code);
        long contentLength = response.contentLength();

        if (!checkIsSupportPartial(response, taskInfo)) {
            boolean delete = DownloadFileHelper.deleteFile(taskInfo.getFilePath());
            L.d("not support partial content, delete file " + delete);
            taskInfo.setCurrentSize(0);
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
            if (totalSize == 0) {
                taskInfo.setTotalSize(response.contentLength() + taskInfo.getCurrentSize());
            }
            handleDownload(response, taskInfo);
        } else if (contentLength <= 0 && (code == Constants.ResponseCode.OK || code == Constants.ResponseCode.PARTIAL_CONTENT)) {
            //无法获取到文件长度的下载情况
            taskInfo.setTotalSize(-1L);
            handleDownload(response, taskInfo);
        } else if (code == Constants.ResponseCode.NOT_FOUND) {
            //未找到文件
            downloadCallback.onFailure(taskInfo);
        } else {
            if (!retryDownload()) {
                downloadCallback.onFailure(taskInfo);
            }
        }
    }

    @Override
    TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public void onFailure(HttpCall call, Exception e) {
        this.call = call;
        if (!retryDownload()) {
            downloadCallback.onFailure(taskInfo);
        }
    }

    private boolean checkIsSupportPartial(HttpResponse response, TaskInfo taskInfo) {
        if (!TextUtils.equals(response.url(), taskInfo.getCacheTargetUrl())) {
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
        String filePath = fixFilePath(response, taskInfo);
        final long currentSize = taskInfo.getCurrentSize();
        final long totalSize = taskInfo.getTotalSize();
        mFileWrite = new SingleFileWriteTask(filePath, currentSize, totalSize);
        mFileWrite.write(response, new SingleFileWriteListener(totalSize, currentSize));
    }

    private String fixFilePath(HttpResponse response, TaskInfo taskInfo) {
        String fileDir = taskInfo.getFileDir();
        String filePath = taskInfo.getFilePath();
        if (!TextUtils.isEmpty(filePath)) {
            String contentDisposition = response.header(NetworkHelper.CONTENT_DISPOSITION);
            String contentType = response.header(NetworkHelper.CONTENT_TYPE);
            String fileName = URLUtil.guessFileName(response.url(), contentDisposition, contentType);
            if (TextUtils.isEmpty(fileName)) {
                fileName = DownloadFileHelper.string2MD5(taskInfo.getResKey());
            }
            filePath = fileDir + File.separator + fileName;
            taskInfo.setFilePath(filePath);
        }
        return filePath;
    }

    private class SingleFileWriteListener implements FileWrite.FileWriteListener {

        long totalSize;

        long currentSize;

        int oldProgress;

        SingleFileWriteListener(long totalSize, long currentSize) {
            this.totalSize = totalSize;
            this.currentSize = currentSize;
        }


        @Override
        public void onWriteFile(long writeLength) {
            if (writeLength > 0) {
                currentRetryTimes = 0;
                currentSize += writeLength;
                taskInfo.setCurrentSize(currentSize);
                int progress = ProgressHelper.computeProgress(currentSize, totalSize);
                taskInfo.setProgress(progress);
                if (!cancel) {
                    if (progress != oldProgress) {
                        downloadCallback.onDownloading(taskInfo);
                    }
                }
                oldProgress = progress;
            }
        }

        @Override
        public void onWriteFinish() {
            taskInfo.setProgress(100);
            downloadCallback.onSuccess(taskInfo);
        }

        @Override
        public void onWriteFailure() {
            if (!retryDownload()) {
                fixCurrentSize();
                downloadCallback.onFailure(taskInfo);
            }
        }
    }


    private boolean retryDownload() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        if (cancel) {
            return false;
        }
        if (currentRetryTimes >= RETRY_MAX_TIMES || totalRetryTimes >= TOTAL_RETRY_MAX_TIMES) {
            return false;
        }

        currentRetryTimes++;
        totalRetryTimes++;

        if (waitingSuitableNetworkType()) {
            if (currentRetryTimes == 0 || currentRetryTimes == 1) {
                SystemClock.sleep(RETRY_DELAY);
            }
            if (currentRetryTimes == 2) {
                SystemClock.sleep(2 * RETRY_DELAY);
            }
            if (cancel) {
                return false;
            }
            fixCurrentSize();
            HttpCall call = client.newCall(taskInfo.getRequestUrl(), taskInfo.getRequestUrl(), taskInfo.getCurrentSize());
            call.enqueue(this);
            return true;
        } else {
            return retryDownload();
        }
    }

    private void fixCurrentSize() {
        String filePath = taskInfo.getFilePath();
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            long length = file.length();
            taskInfo.setCurrentSize(length);
            taskInfo.setProgress(ProgressHelper.computeProgress(length, taskInfo.getTotalSize()));
        }
        taskInfo.setCurrentSize(0);
        taskInfo.setProgress(0);
    }

    private boolean waitingSuitableNetworkType() {
        int waitingNumber = 0;
        while (true) {
            if (cancel) {
                return false;
            }
            if (isSuitableNetworkType()) {
                return true;
            }
            SystemClock.sleep(RETRY_DELAY);
            waitingNumber++;
            if (waitingNumber == SEARCH_WIFI_MAX_TIMES) {
                return false;
            }
        }
    }

    private boolean isSuitableNetworkType() {
        return NetworkHelper.isWifiEnv(context)
                || taskInfo.isPermitRetryInMobileData() && NetworkHelper.isNetEnv(context);
    }

    @Override
    void cancel() {
        L.d("SingleHttpCallbackImpl cancel");
        this.cancel = true;
        if (mFileWrite != null) {
            mFileWrite.stop();
        }
        if (this.call != null && !this.call.isCanceled()) {
            this.call.cancel();
        }
    }
}
