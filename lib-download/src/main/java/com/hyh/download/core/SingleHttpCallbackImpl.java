package com.hyh.download.core;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.hyh.download.Callback;
import com.hyh.download.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.NetworkHelper;

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

    private Callback downloadCallback;

    /**
     * 是否支持断点续传
     */
    private boolean isSupportPartialContent = true;

    private volatile boolean pause;

    private volatile boolean delete;

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

    SingleHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, Callback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.downloadCallback = downloadCallback;
    }


    @Override
    public void onResponse(HttpCall call, HttpResponse response) throws IOException {
        this.call = call;
        if (delete || pause) {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            return;
        }
        int code = response.code();
        taskInfo.setResponseCode(code);
        long contentLength = response.contentLength();

        if (code == Constants.ResponseCode.OK && taskInfo.getCurrentSize() > 0 &&
                (taskInfo.getTotalSize() != 0 && contentLength == taskInfo.getTotalSize())) {
            isSupportPartialContent = false;
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
            //无法获取到文件长度的下载情况，简直坑爹
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

    private void handleDownload(HttpResponse response, final TaskInfo taskInfo) {
        final long currentSize = taskInfo.getCurrentSize();
        final long totalSize = taskInfo.getTotalSize();
        String fileDir = taskInfo.getFileDir();
        String filePath = taskInfo.getFilePath();
        if (!TextUtils.isEmpty(filePath)) {
            String contentDisposition = response.header(NetworkHelper.CONTENT_DISPOSITION);
            String fileName = NetworkHelper.parseContentDisposition(contentDisposition);

        }
        mFileWrite = new SingleFileWriteTask(taskInfo.getFilePath(), currentSize, totalSize);
        mFileWrite.write(response, new SingleFileWriteListener(totalSize, currentSize));
    }

    private class SingleFileWriteListener implements FileWrite.FileWriteListener {

        long totalSize;

        long oldSize;

        int oldProgress;

        SingleFileWriteListener(long totalSize, long currentSize) {
            this.totalSize = totalSize;
            this.oldSize = currentSize;
            this.oldProgress = (totalSize == -1) ? 0 : Math.round(oldSize * 100.0f / totalSize);
        }

        @Override
        public void onWriteFile(long writeLength) {
            if (writeLength > 0) {
                currentRetryTimes = 0;
                if (oldSize == 0 && !pause && !delete) {
                    downloadCallback.onFirstFileWrite(taskInfo);
                }

                oldSize += writeLength;
                taskInfo.setCurrentSize(oldSize);

                if (totalSize == -1) {
                    if (oldProgress != -1) {
                        taskInfo.setProgress(-1);
                        downloadCallback.onDownloading(taskInfo);
                    }
                    oldProgress = -1;
                } else {
                    int progress = Math.round(oldSize * 100.0f / totalSize);
                    if (progress != oldProgress) {
                        currentRetryTimes = 0;
                        taskInfo.setProgress(progress);
                        if (!pause && !delete) {
                            downloadCallback.onDownloading(taskInfo);
                        }
                        oldProgress = progress;
                    }
                }
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
                downloadCallback.onFailure(taskInfo);
            }
        }
    }


    private boolean retryDownload() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        if (pause || delete) {
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
            if (pause || delete) {
                return false;
            }

            long currentSize = fixCurrentSize();
            taskInfo.setCurrentSize(currentSize);
            HttpCall call = client.newCall(taskInfo.getRequestUrl(), taskInfo.getRequestUrl(), currentSize);
            call.enqueue(this);
            return true;
        } else {
            return retryDownload();
        }
    }

    private long fixCurrentSize() {
        if (isSupportPartialContent) {
            String filePath = taskInfo.getFilePath();
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                return file.length();
            }
        } else {
            String filePath = taskInfo.getFilePath();
            DownloadFileHelper.deleteFile(filePath);
        }
        return 0;
    }

    private boolean waitingSuitableNetworkType() {
        int waitingNumber = 0;
        while (true) {
            if (pause || delete) {
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
                || taskInfo.isPermitMobileDataRetry() && NetworkHelper.isNetEnv(context);
    }

    @Override
    void pause() {
        L.d("SingleHttpCallbackImpl pause");
        this.pause = true;
        if (mFileWrite != null) {
            mFileWrite.stop();
        }
        if (this.call != null && !this.call.isCanceled()) {
            this.call.cancel();
        }
    }

    @Override
    void delete() {
        L.d("SingleHttpCallbackImpl delete");
        this.delete = true;
        if (mFileWrite != null) {
            mFileWrite.stop();
        }
        if (this.call != null && !this.call.isCanceled()) {
            this.call.cancel();
        }
    }
}
