package com.eric.hyh.tools.download.internal;

import android.content.Context;
import android.os.SystemClock;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.HttpCall;
import com.eric.hyh.tools.download.api.HttpCallback;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.api.HttpResponse;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

    private boolean isCallbackSuccess;

    private boolean isCallbackFailure;

    private int successCount;

    private int failureCount;


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
        Collection<RealHttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
            httpCallback.pause();
        }
    }

    @Override
    protected void delete() {
        Collection<RealHttpCallbackImpl> httpCallbacks = httpCallbackMap.values();
        for (RealHttpCallbackImpl httpCallback : httpCallbacks) {
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

        protected volatile boolean pause;

        protected volatile boolean delete;
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
            if (code == Constans.ResponseCode.OK || code == Constans.ResponseCode.PARTIAL_CONTENT) {//请求数据成功
                long totalSize = taskInfo.getTotalSize();
                if (totalSize == 0) {
                    taskInfo.setTotalSize(response.contentLength() + taskInfo.getCurrentSize());
                }
                handleDownload(response, taskInfo);
            } else if (code == Constans.ResponseCode.NOT_FOUND) {
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
                            failureCount--;
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
                    successCount++;
                    if (!isCallbackSuccess && successCount < httpCallbackMap.size()) {
                        return;
                    }
                    synchronized (MultiHttpCallbackImpl.this) {
                        if (!isCallbackSuccess && (successCount == httpCallbackMap.size())) {
                            isCallbackSuccess = true;
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
            this.pause = true;
            mFileWrite.stop();
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
        }

        @Override
        void delete() {
            this.delete = true;
            mFileWrite.stop();
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
        }

        void retry() {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }

            if (pause || delete || isCallbackFailure) {
                return;
            }

            if (!isFailure && currentRetryTimes >= RETRY_MAX_TIMES) {
                isFailure = true;
                failureCount++;
            }

            if (!isCallbackFailure && failureCount >= httpCallbackMap.size()) {
                synchronized (MultiHttpCallbackImpl.this) {
                    if (!isCallbackFailure && failureCount >= httpCallbackMap.size()) {
                        isCallbackFailure = true;
                        downloadCallback.onFailure(taskInfo);
                        return;
                    }
                }
            }

            if (currentRetryTimes >= RETRY_MAX_TIMES) {
                //TODO 处理请求失败
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                return;
            }


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
                        if (pause || delete || isCallbackFailure) {
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

                    if (pause || delete || isCallbackFailure) {
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
                if (pause || delete || isCallbackFailure) {
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
