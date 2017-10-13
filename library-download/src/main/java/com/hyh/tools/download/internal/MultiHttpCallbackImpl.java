package com.hyh.tools.download.internal;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.hyh.tools.download.api.Callback;
import com.hyh.tools.download.api.HttpCall;
import com.hyh.tools.download.api.HttpCallback;
import com.hyh.tools.download.api.HttpClient;
import com.hyh.tools.download.api.HttpResponse;
import com.hyh.tools.download.bean.TaskInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 * @description
 * @data 2017/7/12
 */
@SuppressWarnings("unchecked")
class MultiHttpCallbackImpl extends AbstractHttpCallback {

    private Context context;

    private HttpClient client;

    private Map<String, RealHttpCallbackImpl> httpCallbackMap;

    private TaskInfo taskInfo;

    private Callback downloadCallback;

    private volatile boolean isAllSuccess;

    private volatile boolean isAllFailure;

    private AtomicInteger successCount = new AtomicInteger();

    private AtomicInteger failureCount = new AtomicInteger();

    private volatile boolean pause;

    private volatile boolean delete;


    MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, Callback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.downloadCallback = downloadCallback;
        this.httpCallbackMap = new HashMap<>();
        int rangeNum = taskInfo.getRangeNum();
        for (int rangeId = 0; rangeId < rangeNum; rangeId++) {
            RealHttpCallbackImpl realHttpCallback = new RealHttpCallbackImpl(rangeId);
            String tag = taskInfo.getResKey().concat("-").concat(String.valueOf(rangeId));
            httpCallbackMap.put(tag, realHttpCallback);
        }
    }

    HttpCallback getHttpCallback(String tag) {
        return httpCallbackMap.get(tag);
    }

    @Override
    TaskInfo getTaskInfo() {
        return null;
    }

    @Override
    protected void pause() {
        this.pause = true;
        Collection<RealHttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        Log.d("FDL_HH", "MultiHttpCallbackImpl pause httpCallbacks'size = " + httpCallbacks.size());
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
            Log.d("FDL_HH", "RealHttpCallbackImpl pause");
            httpCallback.pause();
        }
    }

    @Override
    protected void delete() {
        this.delete = true;
        Log.d("FDL_HH", "MultiHttpCallbackImpl delete");
        Collection<RealHttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
            Log.d("FDL_HH", "RealHttpCallbackImpl delete");
            httpCallback.delete();
        }
    }


    private class RealHttpCallbackImpl extends AbstractHttpCallback {


        //重试的当前次数
        private volatile int currentRetryTimes = 0;
        //重试的最大次数
        private static final int RETRY_MAX_TIMES = 3;
        //每次重试的延时
        private static final long RETRYDELAY = 1000 * 2;
        //获取wifi重试的最大次数
        private static final int SEARCH_WIFI_MAX_TIMES = 15;

        private boolean isFailure;

        private HttpCall call;

        private Timer timer;

        private long startPosition;

        private long endPosition;

        private int rangeId;

        private FileWrite mFileWrite;


        RealHttpCallbackImpl(int rangeId) {
            this.rangeId = rangeId;
            long[] startPositions = taskInfo.getStartPositions();
            long[] endPositions = taskInfo.getEndPositions();
            startPosition = startPositions[rangeId];
            endPosition = endPositions[rangeId];
        }

        @Override
        public void onResponse(HttpCall httpCall, HttpResponse response) throws IOException {
            this.call = httpCall;
            if (delete || pause) {
                if (this.call != null && !this.call.isCanceled()) {
                    this.call.cancel();
                }
                return;
            }
            int code = response.code();
            taskInfo.setCode(code);
            if (code == Constants.ResponseCode.OK || code == Constants.ResponseCode.PARTIAL_CONTENT) {//请求数据成功
                handleDownload(response, taskInfo);
            } else if (code == Constants.ResponseCode.NOT_FOUND) {
                // TODO: 2017/5/16 未找到文件
                if (downloadCallback != null) {
                    downloadCallback.onFailure(taskInfo);
                }
            } else {
                retry();
            }
        }


        private void handleDownload(HttpResponse response, final TaskInfo taskInfo) throws IOException {
            String filePath = taskInfo.getFilePath();
            String tempPath = filePath.concat("-").concat(String.valueOf(rangeId));
            final long totalSize = taskInfo.getTotalSize();


            File downLoadFile = Utils.getDownLoadFile(context, taskInfo.getResKey());
            if (!downLoadFile.exists() || downLoadFile.length() < totalSize) {
                RandomAccessFile raf = new RandomAccessFile(downLoadFile, "rw");
                raf.setLength(totalSize);
                raf.close();
            }


            mFileWrite = new MultiFileWriteTask(filePath, tempPath, startPosition, endPosition);
            mFileWrite.write(response, new SingleFileWriteTask.FileWriteListener() {

                @Override
                public void onWriteFile(long writeLength) {
                    if (taskInfo.getCurrentSize() == 0 && writeLength > 0) {
                        downloadCallback.onFirstFileWrite(taskInfo);
                    }

                    startPosition += writeLength;

                    int oldProgress = taskInfo.getProgress();
                    long currentSize = taskInfo.getCurrentSize() + writeLength;
                    taskInfo.setCurrentSize(currentSize);
                    int progress = (int) ((currentSize * 100.0f / totalSize) + 0.5f);
                    if (progress != oldProgress) {
                        if (isFailure) {
                            failureCount.decrementAndGet();
                        }
                        isFailure = false;
                        currentRetryTimes = 0;
                        taskInfo.setProgress(progress);
                        if (!pause && !delete) {
                            downloadCallback.onDownloading(taskInfo);
                        }
                    }
                }

                @Override
                public void onWriteFinish() {
                    successCount.incrementAndGet();
                    if (!isAllSuccess && successCount.get() < httpCallbackMap.size()) {
                        return;
                    }

                    synchronized (MultiHttpCallbackImpl.this) {
                        if (!isAllSuccess && (successCount.get() == httpCallbackMap.size())) {
                            isAllSuccess = true;
                            downloadCallback.onSuccess(taskInfo);
                        }
                    }
                }

                @Override
                public void onWriteFailure() {
                    if (!pause && !delete) {
                        retry();
                    }
                }
            });
        }


        @Override
        public void onFailure(HttpCall httpCall, IOException e) {
            this.call = httpCall;
            retry();
        }


        @Override
        TaskInfo getTaskInfo() {
            return taskInfo;
        }

        @Override
        void pause() {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            if (mFileWrite != null) {
                mFileWrite.stop();
            }
        }

        @Override
        void delete() {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            if (mFileWrite != null) {
                mFileWrite.stop();
            }
        }

        void retry() {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }

            if (pause || delete || isAllFailure) {
                return;
            }

            if (!isFailure && currentRetryTimes >= RETRY_MAX_TIMES) {
                isFailure = true;
                failureCount.incrementAndGet();
            }

            if (!isAllFailure && failureCount.get() >= httpCallbackMap.size()) {
                synchronized (MultiHttpCallbackImpl.this) {
                    if (!isAllFailure && failureCount.get() >= httpCallbackMap.size()) {
                        isAllFailure = true;
                        downloadCallback.onFailure(taskInfo);
                        return;
                    }
                }
            }

/*            if (currentRetryTimes >= RETRY_MAX_TIMES) {
                //TODO 处理请求失败
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                return;
            }*/


            timer = new Timer("retry timer");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isWifiOk(context)) {
                        if (currentRetryTimes == 0 || currentRetryTimes == 1) {
                            SystemClock.sleep(2 * 1000);
                        }
                        if (currentRetryTimes == 2) {
                            SystemClock.sleep(4 * 1000);
                        }
                        if (pause || delete || isAllFailure) {
                            timer.cancel();
                            timer = null;
                            return;
                        }
                        HttpCall call = client.newCall(taskInfo.getResKey().concat("-").concat(String.valueOf(rangeId)),
                                taskInfo.getUrl(),
                                startPosition,
                                endPosition);
                        call.enqueue(RealHttpCallbackImpl.this);
                        timer.cancel();
                        timer = null;
                    }
                    currentRetryTimes++;

                    if (pause || delete || isAllFailure) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                    }
                }
            }, RETRYDELAY);
        }


        private boolean isWifiOk(Context context) {
            int count = 0;
            while (true) {
                if (pause || delete || isAllFailure) {
                    return false;
                }
                if (Utils.isWifi(context)) {
                    return true;
                }
                SystemClock.sleep(2000);
                count++;
                if (count == SEARCH_WIFI_MAX_TIMES) {
                    return false;
                }
            }
        }
    }
}
