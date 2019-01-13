package com.hyh.download.core;

import android.content.Context;
import android.os.SystemClock;

import com.hyh.download.FailureCode;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.exception.ExceptionHelper;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpCallback;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;
import com.hyh.download.utils.RangeUtil;

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
class MultiHttpCallbackWrapper extends AbstractHttpCallback {

    private final Object mLock = new Object();

    private final Context context;

    private final HttpClient client;

    private final TaskInfo taskInfo;

    private final long totalSize;

    private final AtomicLong currentSize;

    private final AtomicInteger currentProgress;

    private final DownloadCallback downloadCallback;

    private final Map<String, RealHttpCallbackImpl> httpCallbackMap;

    private volatile long lastPostTimeMillis;

    private int taskNum;

    private List<RangeInfo> rangeInfoList;

    private volatile AtomicInteger retryingCount = new AtomicInteger();

    private volatile AtomicInteger successCount = new AtomicInteger();

    private volatile AtomicInteger failureCount = new AtomicInteger();

    private volatile boolean cancel;

    private volatile boolean isNotifySuccess;

    private volatile boolean isNotifyFailure;

    MultiHttpCallbackWrapper(Context context,
                             HttpClient client,
                             TaskInfo taskInfo,
                             DownloadCallback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.totalSize = taskInfo.getTotalSize();
        this.currentSize = new AtomicLong(taskInfo.getCurrentSize());
        this.currentProgress = new AtomicInteger(RangeUtil.computeProgress(taskInfo.getCurrentSize(), totalSize));
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
    protected void cancel() {
        this.cancel = true;
        Collection<RealHttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
            httpCallback.cancel();
        }
    }

    private boolean isAllRetrying() {
        return retryingCount.get() == taskNum;
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

        notifyDownloading(curSize, this.totalSize);
    }


    private void handleRetrying(int failureCode, boolean deleteFile) {
        synchronized (mLock) {
            if (isAllRetrying()) {
                notifyRetrying(failureCode, deleteFile);
            }
        }
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
                notifySuccess();
            }
        }
    }

    private void handleWriteFailure(int failureCode) {
        synchronized (mLock) {
            failureCount.incrementAndGet();
            if (isAllFailure()) {
                notifyFailure(failureCode);
            }
        }
    }

    private void notifyDownloading(long curSize, long totalSize) {
        long elapsedTimeMillis = SystemClock.elapsedRealtime();
        int oldProgress = currentProgress.get();
        int curProgress = RangeUtil.computeProgress(curSize, totalSize);
        if (oldProgress != curProgress) {
            currentProgress.set(curProgress);

            downloadCallback.onDownloading(curSize);

            lastPostTimeMillis = elapsedTimeMillis;
        } else {
            long diffTimeMillis = elapsedTimeMillis - lastPostTimeMillis;
            if (diffTimeMillis >= 2000) {

                downloadCallback.onDownloading(curSize);

                lastPostTimeMillis = elapsedTimeMillis;
            }
        }
    }

    private void notifyRetrying(int failureCode, boolean deleteFile) {
        downloadCallback.onRetrying(failureCode, deleteFile);
    }

    private void notifySuccess() {
        fixCurrentSize();
        if (!isNotifySuccess && !cancel) {
            downloadCallback.onSuccess();
            isNotifySuccess = true;
        }
    }

    private void notifyFailure(int failureCode) {
        fixCurrentSize();
        if (!isNotifyFailure && !cancel) {
            downloadCallback.onFailure(failureCode);
            isNotifyFailure = true;
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
        }
    }

    private void setCurrentSize(long currentSize) {
        synchronized (mLock) {
            taskInfo.setCurrentSize(currentSize);
        }
    }

    private class RealHttpCallbackImpl extends AbstractHttpCallback {

        private final Object mLock = new Object();

        private final RangeInfo rangeInfo;

        private final IRetryStrategy retryStrategy;

        private volatile boolean isFailure;

        private volatile int failureCode;

        private HttpCall call;

        private FileWrite fileWrite;

        RealHttpCallbackImpl(RangeInfo rangeInfo) {
            this.rangeInfo = rangeInfo;
            this.retryStrategy = new RetryStrategyImpl(context, taskInfo.isPermitRetryInMobileData());
        }

        void wake() {
            if (isFailure) {
                synchronized (mLock) {
                    if (isFailure) {
                        failureCount.decrementAndGet();
                        isFailure = false;
                    }
                }
                retryStrategy.clearCurrentRetryTimes();
                boolean retryDownload = retryDownload(failureCode, false);
                if (!retryDownload) {
                    synchronized (mLock) {
                        this.isFailure = true;
                        handleWriteFailure(failureCode);
                    }
                }
            }
        }

        @Override
        public void onResponse(HttpCall httpCall, HttpResponse response) {
            this.call = httpCall;
            if (cancel) {
                if (this.call != null && !this.call.isCanceled()) {
                    this.call.cancel();
                }
                return;
            }
            int code = response.code();
            taskInfo.setResponseCode(code);

            if (!response.isSuccessful()) {
                failureCode = FailureCode.HTTP_ERROR;
                if (!retryDownload(failureCode, false)) {
                    synchronized (mLock) {
                        this.isFailure = true;
                        handleWriteFailure(failureCode);
                    }
                }
                return;
            }

            handleDownload(response, taskInfo);
        }

        @Override
        public void onFailure(HttpCall httpCall, Exception e) {
            this.call = httpCall;
            failureCode = ExceptionHelper.convertFailureCode(e);
            if (!retryDownload(failureCode, false)) {
                synchronized (mLock) {
                    isFailure = true;
                    handleWriteFailure(failureCode);
                }
            }
        }

        private void handleDownload(HttpResponse response, final TaskInfo taskInfo) {
            String fileDir = taskInfo.getFileDir();
            DownloadFileHelper.ensureCreated(fileDir);
            fileWrite = new MultiFileWriteTask(DownloadFileHelper.getTaskFilePath(taskInfo), rangeInfo);
            fileWrite.write(response, new MultiFileWriteListener());
        }


        private class MultiFileWriteListener implements FileWrite.FileWriteListener {

            MultiFileWriteListener() {
            }

            @Override
            public void onWriteFile(long writeLength) {
                retryStrategy.clearCurrentRetryTimes();
                rangeInfo.addStartPosition(writeLength);
                MultiHttpCallbackWrapper.this.handleWriteFile(writeLength);
            }

            @Override
            public void onWriteFinish() {
                MultiHttpCallbackWrapper.this.handleWriteFinish();
            }

            @Override
            public void onWriteFailure(Exception e) {
                failureCode = ExceptionHelper.convertFailureCode(e);
                if (!retryDownload(failureCode, false)) {
                    isFailure = true;
                    MultiHttpCallbackWrapper.this.handleWriteFailure(failureCode);
                }
            }

            @Override
            public void onWriteLengthError(long startPosition, long endPosition) {
                L.d("RealHttpCallbackImpl: onWriteLengthError startPosition = " + startPosition + ", endPosition = " + endPosition);
                long oldStartPosition = rangeInfo.getStartPosition();
                long curStartPosition = 0;
                long fixDiff = curStartPosition - oldStartPosition;

                rangeInfo.addStartPosition(fixDiff);
                addCurrentSize(fixDiff);

                RangeUtil.writeStartPosition(rangeInfo);

                failureCode = FailureCode.FILE_LENGTH_ERROR;
                if (!retryDownload(failureCode, false)) {
                    isFailure = true;
                    MultiHttpCallbackWrapper.this.handleWriteFailure(failureCode);
                }
            }
        }

        @Override
        void cancel() {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            if (fileWrite != null) {
                fileWrite.stop();
            }
        }

        boolean retryDownload(final int failureCode, final boolean deleteFile) {
            synchronized (mLock) {
                retryingCount.incrementAndGet();
            }

            if (call != null && !call.isCanceled()) {
                call.cancel();
            }


            handleRetrying(failureCode, deleteFile);
            boolean shouldRetry = retryStrategy.shouldRetry();
            if (shouldRetry) {

                long oldStartPosition = rangeInfo.getStartPosition();
                long curStartPosition = fixStartPosition(oldStartPosition);
                long fixDiff = curStartPosition - oldStartPosition;

                rangeInfo.addStartPosition(fixDiff);
                addCurrentSize(fixDiff);

                HttpCall call = client.newCall(taskInfo.getResKey().concat("-").concat(String.valueOf(rangeInfo.getRangeIndex())),
                        taskInfo.getRequestUrl(),
                        curStartPosition,
                        rangeInfo.getEndPosition());

                call.enqueue(RealHttpCallbackImpl.this);
            }

            synchronized (mLock) {
                retryingCount.decrementAndGet();
            }
            return shouldRetry;
        }

        private long fixStartPosition(long oldStartPosition) {
            return RangeUtil.fixStartPosition(
                    DownloadFileHelper.getTaskFilePath(taskInfo),
                    oldStartPosition,
                    rangeInfo.getOriginalStartPosition(),
                    rangeInfo.getEndPosition());
        }
    }
}