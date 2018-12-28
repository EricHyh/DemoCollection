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
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.ProgressHelper;
import com.hyh.download.utils.StreamUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final DownloadCallback downloadCallback;

    private final int rangeNum;

    private List<RangeInfo> rangeInfoList;

    private volatile AtomicInteger successCount = new AtomicInteger();

    private volatile AtomicInteger failureCount = new AtomicInteger();

    private volatile boolean cancel;

    private volatile boolean isCallbackSuccess;

    private volatile boolean isCallbackFailure;

    private final ThreadPoolExecutor executor;

    {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(10);
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "MultiHttpCallbackImpl");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                L.d("RealHttpCallbackImpl rejectedExecution");
                if (r instanceof OnWriteFileTask) {
                    OnWriteFileTask task = (OnWriteFileTask) r;
                    if (!executor.isShutdown()) {
                        OnWriteFileTask pollTask = (OnWriteFileTask) executor.getQueue().poll();
                        if (pollTask != null) {
                            task.mergeTask(pollTask);
                        }
                        executor.execute(task);
                    }
                }
            }
        };
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, workQueue, threadFactory, handler);
        executor.allowCoreThreadTimeOut(true);
    }


    MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, DownloadCallback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.totalSize = taskInfo.getTotalSize();
        this.downloadCallback = downloadCallback;
        this.httpCallbackMap = new HashMap<>();
        this.rangeNum = taskInfo.getRangeNum();

        int size = httpCallbackMap.size();
        successCount.set(rangeNum - size);
    }

    void setRangeInfoList(List<RangeInfo> rangeInfoList) {
        this.rangeInfoList = rangeInfoList;
        for (RangeInfo rangeInfo : rangeInfoList) {
            long startPosition = rangeInfo.getStartPosition();
            long endPosition = rangeInfo.getEndPosition();
            if (startPosition < endPosition) {
                RealHttpCallbackImpl realHttpCallback = new RealHttpCallbackImpl(rangeInfo);
                String tag = taskInfo.getResKey().concat("-").concat(String.valueOf(rangeInfo.getRangeIndex()));
                httpCallbackMap.put(tag, realHttpCallback);
            }
        }
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
        L.d("MultiHttpCallbackImpl cancel httpCallbacks'size = " + httpCallbacks.size());
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
            L.d("RealHttpCallbackImpl cancel");
            httpCallback.cancel();
        }
    }

    private boolean isAllSuccess() {
        return successCount.get() == rangeNum;
    }

    private boolean isAllFailure() {
        return failureCount.get() == rangeNum;
    }

    private void handleWriteFile(long writeLength) {

        if (!cancel && failureCount.get() > 0) {
            Collection<RealHttpCallbackImpl> values = httpCallbackMap.values();
            for (RealHttpCallbackImpl httpCallback : values) {
                httpCallback.wake();
            }
        }


        executor.execute(new OnWriteFileTask(writeLength));
    }

    private void handleWriteFinish() {
        synchronized (mLock) {
            successCount.incrementAndGet();
            if (isAllSuccess()) {
                fixCurrentSize();
                if (!isCallbackSuccess) {
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

            int progress = ProgressHelper.computeProgress(currentSize, totalSize);
            taskInfo.setProgress(progress);
        }
    }

    private void setCurrentSize(long currentSize) {
        synchronized (mLock) {
            taskInfo.setCurrentSize(currentSize);

            int progress = ProgressHelper.computeProgress(currentSize, totalSize);
            taskInfo.setProgress(progress);
        }
    }

    private class RealHttpCallbackImpl extends AbstractHttpCallback {

        private final Object mLock = new Object();

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

        private final RangeInfo rangeInfo;

        private volatile boolean isFailure;

        private HttpCall call;

        private FileWrite mFileWrite;

        RealHttpCallbackImpl(RangeInfo rangeInfo) {
            this.rangeInfo = rangeInfo;
        }

        void wake() {
            if (isFailure) {
                synchronized (mLock) {
                    if (isFailure) {
                        isFailure = false;
                        currentRetryTimes = 0;
                        totalRetryTimes -= 3;
                        boolean retryDownload = retryDownload();
                        if (retryDownload) {
                            failureCount.decrementAndGet();
                        }
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
                if (writeLength > 0) {
                    rangeInfo.addStartPosition(writeLength);
                    currentRetryTimes = 0;
                    MultiHttpCallbackImpl.this.handleWriteFile(writeLength);
                }
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
            if (cancel || isAllFailure()) {
                return false;
            }
            if (currentRetryTimes >= RETRY_MAX_TIMES || totalRetryTimes >= TOTAL_RETRY_MAX_TIMES) {
                return false;
            }
            currentRetryTimes++;
            if (waitingSuitableNetworkType()) {
                if (currentRetryTimes == 0 || currentRetryTimes == 1) {
                    SystemClock.sleep(RETRY_DELAY);
                }
                if (currentRetryTimes == 2) {
                    SystemClock.sleep(2 * RETRY_DELAY);
                }
                if (cancel || isAllFailure()) {
                    return false;
                }


                long oldStartPosition = rangeInfo.getStartPosition();
                long curStartPosition = fixStartPosition(oldStartPosition);

                addCurrentSize(curStartPosition - oldStartPosition);

                HttpCall call = client.newCall(taskInfo.getResKey().concat("-").concat(String.valueOf(rangeInfo.getRangeIndex())),
                        taskInfo.getRequestUrl(),
                        curStartPosition,
                        rangeInfo.getEndPosition());

                call.enqueue(RealHttpCallbackImpl.this);
                return true;
            } else {
                return retryDownload();
            }
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

        private long fixStartPosition(long oldStartPosition) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(taskInfo.getFilePath(), "rw");
                for (; ; ) {
                    raf.seek(oldStartPosition);
                    if (raf.readByte() == 0) {
                        oldStartPosition--;
                        if (oldStartPosition <= rangeInfo.getOriginalStartPosition()) {
                            oldStartPosition = rangeInfo.getOriginalStartPosition();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                //
            }
            StreamUtil.close(raf);
            return oldStartPosition;
        }
    }

    private class OnWriteFileTask implements Runnable {

        private volatile long writeLength;

        private volatile int oldProgress;

        OnWriteFileTask(long writeLength) {
            this.writeLength = writeLength;
        }

        void mergeTask(OnWriteFileTask task) {
            this.writeLength += task.writeLength;
        }

        @Override
        public void run() {
            synchronized (mLock) {
                if (cancel || isAllSuccess() || isAllSuccess()) {
                    return;
                }
                addCurrentSize(writeLength);
                int progress = taskInfo.getProgress();
                if (progress != oldProgress) {
                    downloadCallback.onDownloading(taskInfo);
                }
                oldProgress = progress;
            }
        }
    }
}