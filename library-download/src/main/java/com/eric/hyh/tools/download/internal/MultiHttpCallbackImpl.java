package com.eric.hyh.tools.download.internal;

import android.content.Context;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.HttpCall;
import com.eric.hyh.tools.download.api.HttpCallback;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.api.HttpResponse;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/7/12
 */

class MultiHttpCallbackImpl extends AbstractHttpCallback {

    private Map<String, RealHttpCallbackImpl> httpCallbackMap;


    MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, Callback downloadCallback) {

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


    static class RealHttpCallbackImpl extends AbstractHttpCallback {

        private HttpCall call;

        private TaskInfo taskInfo;

        private Callback downloadCallback;

        private volatile int currentRetryTimes = 0;

        protected volatile boolean pause;

        protected volatile boolean delete;


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


        private void handleDownload(HttpResponse response, final TaskInfo taskInfo) {
            final long currentSize = taskInfo.getCurrentSize();
            final long totalSize = taskInfo.getTotalSize();

            FileWrite fileWrite = new MultiFileWriteTask(taskInfo.getFilePath(), currentSize, totalSize);
            fileWrite.write(response, new SingleFileWriteTask.FileWriteListener() {

                long oldSize = currentSize;

                int oldProgress = (int) ((oldSize * 100.0f / totalSize) + 0.5f);

                @Override
                public void onWriteFile(long currentSize) {
                    if (oldSize == 0 && currentSize > 0) {
                        if (downloadCallback != null) {
                            downloadCallback.onFirstFileWrite(taskInfo);
                        }
                        taskInfo.setCurrentSize(currentSize);
                        int progress = (int) ((currentSize * 100.0f / totalSize) + 0.5f);
                        if (progress != oldProgress) {
                            currentRetryTimes = 0;
                            taskInfo.setProgress(progress);
                            if (!pause && !delete && downloadCallback != null) {
                                downloadCallback.onDownloading(taskInfo);
                            }
                            oldProgress = progress;
                        }
                    }
                }

                @Override
                public void onWriteFinish() {
                    if (downloadCallback != null) {
                        downloadCallback.onSuccess(taskInfo);
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
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
        }

        @Override
        void delete() {
            this.delete = true;
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
        }

        private void retry() {

        }
    }
}
