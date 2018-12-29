package com.hyh.download.core;

import android.content.Context;
import android.os.SystemClock;

import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpCallback;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.RangeUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Administrator
 * @description
 * @data 2017/7/12
 */
@SuppressWarnings("unchecked")
class MultiHttpCallbackImpl extends AbstractHttpCallback {

    private final Object mLock = new Object();

    private final Context context;

    private final HttpClient client;

    private final Map<String, RealHttpCallbackImpl> httpCallbackMap;

    private final TaskInfo taskInfo;

    private final long totalSize;

    private final AtomicLong currentSize;

    private volatile long lastPostTimeMillis;

    private final DownloadCallback downloadCallback;

    private int taskNum;

    private List<RangeInfo> rangeInfoList;

    private volatile AtomicInteger successCount = new AtomicInteger();

    private volatile AtomicInteger failureCount = new AtomicInteger();

    private volatile boolean cancel;

    private volatile boolean isCallbackSuccess;

    private volatile boolean isCallbackFailure;

    MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, DownloadCallback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.totalSize = taskInfo.getTotalSize();
        this.currentSize = new AtomicLong(taskInfo.getCurrentSize());
        this.downloadCallback = downloadCallback;
        this.httpCallbackMap = new HashMap<>();
    }

    void setRangeInfoList(List<RangeInfo> rangeInfoList) {
        this.rangeInfoList = rangeInfoList;
        for (RangeInfo rangeInfo : rangeInfoList) {
            long startPosition = rangeInfo.getStartPosition();
            long endPosition = rangeInfo.getEndPosition();
            if (startPosition <= endPosition) {
                RealHttpCallbackImpl realHttpCallback = new RealHttpCallbackImpl(rangeInfo);
                String tag = taskInfo.getResKey().concat("-").concat(String.valueOf(rangeInfo.getRangeIndex()));
                httpCallbackMap.put(tag, realHttpCallback);
            }
        }
        this.taskNum = httpCallbackMap.size();
    }

    HttpCallback getHttpCallback(String tag) {
        return httpCallbackMap.get(tag);
    }

    @Override
    TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    protected void cancel() {
        this.cancel = true;
        Collection<RealHttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
            httpCallback.cancel();
        }
    }

    private boolean isAllSuccess() {
        return successCount.get() == taskNum;
    }

    private boolean isAllFailure() {
        return failureCount.get() == taskNum;
    }

    private void handleWriteFile(long writeLength) {
        long curSize = currentSize.addAndGet(writeLength);
        taskInfo.setCurrentSize(curSize);

        long elapsedTimeMillis = SystemClock.elapsedRealtime();
        long diffTimeMillis = elapsedTimeMillis - lastPostTimeMillis;
        if (diffTimeMillis >= 2000) {
            taskInfo.setProgress(RangeUtil.computeProgress(curSize, totalSize));
            downloadCallback.onDownloading(taskInfo);
            lastPostTimeMillis = elapsedTimeMillis;
        }
    }

    private void handleRetry() {
        taskInfo.setProgress(RangeUtil.computeProgress(currentSize.get(), totalSize));
        downloadCallback.onDownloading(taskInfo);
    }

    private void handleWriteFinish() {
        if (!cancel && failureCount.get() > 0) {
            Collection<RealHttpCallbackImpl> values = httpCallbackMap.values();
            for (RealHttpCallbackImpl httpCallback : values) {
                httpCallback.wake();
            }
        }
        synchronized (mLock) {
            successCount.incrementAndGet();
            if (isAllSuccess()) {
                fixCurrentSize();
                if (!isCallbackSuccess) {
                    L.d("onSuccess");
                    downloadCallback.onSuccess(taskInfo);
                    isCallbackSuccess = true;
                }
            }
        }
    }

    private void handleWriteFailure() {
        synchronized (mLock) {
            failureCount.incrementAndGet();
            if (isAllFailure()) {
                fixCurrentSize();
                if (!isCallbackFailure) {
                    downloadCallback.onFailure(taskInfo);
                    isCallbackFailure = true;
                }
            }
        }
    }

    private void fixCurrentSize() {
        long currentSize = 0;
        for (RangeInfo rangeInfo : rangeInfoList) {
            long originalStartPosition = rangeInfo.getOriginalStartPosition();
            long startPosition = rangeInfo.getStartPosition();
            currentSize += (startPosition - originalStartPosition);
        }
        setCurrentSize(currentSize);
    }

    private void addCurrentSize(long writeLength) {
        synchronized (mLock) {
            long currentSize = taskInfo.getCurrentSize();
            currentSize += writeLength;
            taskInfo.setCurrentSize(currentSize);

            int progress = RangeUtil.computeProgress(currentSize, totalSize);
            taskInfo.setProgress(progress);
        }
    }

    private void setCurrentSize(long currentSize) {
        synchronized (mLock) {
            taskInfo.setCurrentSize(currentSize);

            int progress = RangeUtil.computeProgress(currentSize, totalSize);
            taskInfo.setProgress(progress);
        }
    }

    private class RealHttpCallbackImpl extends AbstractHttpCallback {

        private final Object mLock = new Object();

        private final RangeInfo rangeInfo;

        private final IRetryStrategy mRetryStrategy;

        private volatile boolean isFailure;

        private HttpCall call;

        private FileWrite mFileWrite;

        RealHttpCallbackImpl(RangeInfo rangeInfo) {
            this.rangeInfo = rangeInfo;
            this.mRetryStrategy = new RetryStrategyImpl(context, taskInfo.isPermitRetryInMobileData());
        }

        void wake() {
            if (isFailure) {
                synchronized (mLock) {
                    if (isFailure) {
                        failureCount.decrementAndGet();
                        isFailure = false;
                    }
                }
                mRetryStrategy.clearCurrentRetryTimes();
                boolean retryDownload = retryDownload();
                if (!retryDownload) {
                    synchronized (mLock) {
                        this.isFailure = true;
                        handleWriteFailure();
                    }
                }
            }
        }

        @Override
        public void onResponse(HttpCall httpCall, HttpResponse response) throws IOException {
            this.call = httpCall;
            if (cancel) {
                if (this.call != null && !this.call.isCanceled()) {
                    this.call.cancel();
                }
                return;
            }
            int code = response.code();
            taskInfo.setResponseCode(code);
            if (code == Constants.ResponseCode.OK || code == Constants.ResponseCode.PARTIAL_CONTENT) {//请求数据成功
                handleDownload(response, taskInfo);
            } else if (code == Constants.ResponseCode.NOT_FOUND) {
                //未找到文件
                synchronized (mLock) {
                    this.isFailure = true;
                }
                handleWriteFailure();
            } else {
                if (!retryDownload()) {
                    synchronized (mLock) {
                        this.isFailure = true;
                        handleWriteFailure();
                    }
                }
            }
        }

        @Override
        public void onFailure(HttpCall httpCall, Exception e) {
            this.call = httpCall;
            if (!retryDownload()) {
                synchronized (mLock) {
                    isFailure = true;
                }
                handleWriteFailure();
            }
        }

        private void handleDownload(HttpResponse response, final TaskInfo taskInfo) throws IOException {

            String filePath = taskInfo.getFilePath();
            DownloadFileHelper.ensureParentCreated(filePath);

            /*long totalSize = taskInfo.getTotalSize();
            File downLoadFile = new File(filePath);
            if (!downLoadFile.exists() || downLoadFile.length() < totalSize) {
                RandomAccessFile raf = new RandomAccessFile(downLoadFile, "rw");
                raf.setLength(totalSize);
                raf.close();
            }*/

            mFileWrite = new MultiFileWriteTask(filePath, rangeInfo);
            mFileWrite.write(response, new MultiFileWriteListener());
        }


        private class MultiFileWriteListener implements FileWrite.FileWriteListener {

            MultiFileWriteListener() {
            }

            @Override
            public void onWriteFile(long writeLength) {
                mRetryStrategy.clearCurrentRetryTimes();
                rangeInfo.addStartPosition(writeLength);
                MultiHttpCallbackImpl.this.handleWriteFile(writeLength);
            }

            @Override
            public void onWriteFinish() {
                MultiHttpCallbackImpl.this.handleWriteFinish();
            }

            @Override
            public void onWriteFailure() {
                if (!retryDownload()) {
                    isFailure = true;
                    MultiHttpCallbackImpl.this.handleWriteFailure();
                }
            }
        }

        @Override
        TaskInfo getTaskInfo() {
            return taskInfo;
        }

        @Override
        void cancel() {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            if (mFileWrite != null) {
                mFileWrite.stop();
            }
        }

        boolean retryDownload() {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
            boolean shouldRetry = mRetryStrategy.shouldRetry(new IRetryStrategy.onWaitingListener() {
                @Override
                public void onWaiting() {

                }
            });
            if (shouldRetry) {
                long oldStartPosition = rangeInfo.getStartPosition();
                long curStartPosition = fixStartPosition(oldStartPosition);

                addCurrentSize(curStartPosition - oldStartPosition);

                HttpCall call = client.newCall(taskInfo.getResKey().concat("-").concat(String.valueOf(rangeInfo.getRangeIndex())),
                        taskInfo.getRequestUrl(),
                        curStartPosition,
                        rangeInfo.getEndPosition());

                call.enqueue(RealHttpCallbackImpl.this);
            }
            return shouldRetry;
        }

        private long fixStartPosition(long oldStartPosition) {
            return RangeUtil.fixStartPosition(taskInfo.getFilePath(), oldStartPosition, rangeInfo.getOriginalStartPosition());
        }
    }
}