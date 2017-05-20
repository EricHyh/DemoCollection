package com.eric.hyh.tools.download.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.eric.hyh.tools.download.api.Callback;
import com.eric.hyh.tools.download.api.HttpCall;
import com.eric.hyh.tools.download.api.HttpCallback;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.api.HttpResponse;
import com.eric.hyh.tools.download.bean.Command;
import com.eric.hyh.tools.download.bean.State;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public abstract class SuperDownloadAgent implements IDownloadAgent {

    protected Context context;
    protected HttpClient client;

    SuperDownloadAgent(Context context) {
        this.context = context;
        client = getHttpClient();
    }


    private final int LIMIT_TASK_SIZE = 2;

    private Map<String, HttpCallbackImpl> httpCallbacks = new ConcurrentHashMap<>();

    private List<TaskCache> waitingQueue = new CopyOnWriteArrayList<>();


    HttpCall getCall(String resKey, String url, long oldSize) {
        return client.newCall(resKey, url, oldSize);
    }

    protected abstract HttpClient getHttpClient();


    @Override
    public void enqueue(int command, TaskInfo taskInfo) {
        enqueue(command, taskInfo, null);
    }

    @Override
    public void enqueue(int command, TaskInfo taskInfo, Callback callback) {
        String resKey = taskInfo.getResKey();
        switch (command) {
            case Command.START:
            case Command.UPDATE:
                int runningTasksize = httpCallbacks.size();
                HttpCallbackImpl httpCallbackImpl = httpCallbacks.get(resKey);
                TaskInfo tTaskInfo = null;
                if (httpCallbackImpl != null) {
                    tTaskInfo = httpCallbackImpl.taskInfo;
                }
                if (tTaskInfo != null && !State.canDownload(tTaskInfo.getCurrentStatus())) {//检查状态
                    return;
                }
                if (runningTasksize >= LIMIT_TASK_SIZE) {
                    handleWaitingInQueue(taskInfo, callback);
                    return;
                }
                handlePrepare(taskInfo, null);
                HttpCall call = getCall(resKey, taskInfo.getUrl(), taskInfo.getCurrentSize());
                httpCallbackImpl = new HttpCallbackImpl(call, taskInfo);
                httpCallbackImpl.setCallback(callback);
                httpCallbacks.put(resKey, httpCallbackImpl);
                call.enqueue(httpCallbackImpl);
                break;
            case Command.PAUSE:
                HttpCallbackImpl remove = httpCallbacks.remove(resKey);
                if (remove != null) {
                    remove.setPause(true);
                    if (remove.call != null && !remove.call.isCanceled()) {
                        remove.call.cancel();
                    }
                    if (callback == remove.callback) {
                        handlePause(taskInfo, callback);
                    } else {
                        handlePause(taskInfo, callback, remove.callback);
                    }
                } else {
                    TaskCache taskCache = null;
                    for (TaskCache task : waitingQueue) {
                        if (TextUtils.equals(task.resKey, resKey)) {
                            taskCache = task;
                            break;
                        }
                    }
                    Callback removeCallback = null;
                    if (taskCache != null) {
                        waitingQueue.remove(taskCache);
                        removeCallback = taskCache.callback;
                    }
                    if (callback == removeCallback) {
                        handlePause(taskInfo, callback);
                    } else {
                        handlePause(taskInfo, callback, removeCallback);
                    }
                }
                break;
            case Command.DELETE:
                remove = httpCallbacks.remove(resKey);
                if (remove != null) {
                    remove.setDelete(true);
                    if (remove.call != null && !remove.call.isCanceled()) {
                        remove.call.cancel();
                    }
                    if (callback == remove.callback) {
                        handleDelete(taskInfo, callback);
                    } else {
                        handleDelete(taskInfo, callback, remove.callback);
                    }
                } else {
                    TaskCache taskCache = null;
                    for (TaskCache task : waitingQueue) {
                        if (TextUtils.equals(task.resKey, resKey)) {
                            taskCache = task;
                            break;
                        }
                    }
                    Callback removeCallback = null;
                    if (taskCache != null) {
                        waitingQueue.remove(taskCache);
                        removeCallback = taskCache.callback;
                    }
                    if (callback == removeCallback) {
                        handleDelete(taskInfo, callback);
                    } else {
                        handleDelete(taskInfo, callback, removeCallback);
                    }
                }
                break;
        }
    }


    private void startNextTask() {
        if (!waitingQueue.isEmpty()) {
            TaskCache remove = waitingQueue.remove(0);
            enqueue(Command.START, remove.taskInfo, remove.callback);
        } else {
            if (httpCallbacks.isEmpty()) {
                //TODO 没任务了
                handleHaveNoTask();
                Log.d("DownloadModule", "startNextTask: 没任务了");
            }
        }
    }


    private void handleWaitingInQueue(TaskInfo taskInfo, Callback callback) {
        taskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);
        TaskCache taskCache = new TaskCache(taskInfo.getResKey(), taskInfo, callback);
        waitingQueue.remove(taskCache);
        waitingQueue.add(taskCache);
        handleCallbackAndDB(taskInfo, callback);
    }


    private void handleSuccess(TaskInfo taskInfo, Callback callback) {
        taskInfo.setCurrentStatus(State.SUCCESS);
        String resKey = taskInfo.getResKey();
        httpCallbacks.remove(resKey);
        if (TextUtils.isEmpty(taskInfo.getPackageName())) {
            PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(taskInfo.getFilePath(), 0);
            if (packageInfo != null) {
                taskInfo.setPackageName(packageInfo.packageName);
            }
        }
        handleCallbackAndDB(taskInfo, callback);
        startNextTask();
    }


    private void handleDelete(TaskInfo taskInfo, Callback... callback) {
        taskInfo.setCurrentStatus(State.DELETE);
        handleCallbackAndDB(taskInfo, callback);
        Utils.deleteDownloadFile(context, taskInfo.getResKey());
        startNextTask();
    }

    private void handlePause(TaskInfo taskInfo, Callback... callback) {
        taskInfo.setCurrentStatus(State.PAUSE);
        handleCallbackAndDB(taskInfo, callback);
        startNextTask();
    }


    private void handlePrepare(TaskInfo taskInfo, Callback callback) {
        taskInfo.setCurrentStatus(State.PREPARE);
        handleCallbackAndDB(taskInfo, callback);
    }

    private void handleFirstFileWrite(TaskInfo taskInfo, Callback callback) {
        taskInfo.setCurrentStatus(State.START_WRITE);
        handleCallbackAndDB(taskInfo, callback);
    }

    private void handleFailure(TaskInfo taskInfo, Callback callback) {
        if (taskInfo.isWifiAutoRetry()) {
            taskInfo.setCurrentStatus(State.WAITING_FOR_WIFI);
        } else {
            taskInfo.setCurrentStatus(State.FAILURE);
        }
        String resKey = taskInfo.getResKey();
        httpCallbacks.remove(resKey);
        handleCallbackAndDB(taskInfo, callback);
        startNextTask();
    }


    private void handleDownloading(TaskInfo taskInfo, Callback callback) {
        taskInfo.setCurrentStatus(State.DOWNLOADING);
        handleCallbackAndDB(taskInfo, callback);
    }

    protected abstract void handleHaveNoTask();


    protected abstract void handleCallbackAndDB(TaskInfo taskInfo, Callback... callback);


    /**
     * okhttp请求数据的回调
     */
    private class HttpCallbackImpl implements HttpCallback {


        HttpCall call;

        TaskInfo taskInfo;

        Callback callback;

        Timer timer;

        volatile boolean pause;

        volatile boolean delete;

        //重试的当前次数
        private volatile int currentRetryTimes = 0;
        //重试的最大次数
        private static final int RETRY_MAX_TIMES = 3;
        //每次重试的延时
        private static final long RETRYDELAY = 1000 * 2;
        //获取wifi重试的最大次数
        private static final int SEARCH_WIFI_MAX_TIMES = 15;

        HttpCallbackImpl(HttpCall call, TaskInfo taskInfo) {
            this.call = call;
            this.taskInfo = taskInfo;
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
            taskInfo.setCode(code);
            if (code == Constans.ResponseCode.OK || code == Constans.ResponseCode.PARTIAL_CONTENT) {//请求数据成功
                long totalSize = taskInfo.getTotalSize();
                if (totalSize == 0) {
                    taskInfo.setTotalSize(response.contentLength() + taskInfo.getCurrentSize());
                }
                handleDownload(response, taskInfo, callback);
            } else if (code == Constans.ResponseCode.NOT_FOUND) {
                // TODO: 2017/5/16 未找到文件
                handleFailure(taskInfo, callback);
            } else {
                retry();
            }
        }


        @Override
        public void onFailure(HttpCall call, IOException e) {
            this.call = call;
            retry();
        }

        private void handleDownload(HttpResponse response, TaskInfo taskInfo, Callback callback) throws IOException {
            InputStream inputStream = response.inputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            BufferedOutputStream bos = null;
            long currentSize = taskInfo.getCurrentSize();
            long totalSize = taskInfo.getTotalSize();
            int oldProgress = (int) ((currentSize * 100.0 / totalSize) + 0.5);
            boolean isException = false;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(taskInfo.getFilePath(), true));
                byte[] buffer = new byte[8 * 1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    if (currentSize == 0 && len > 0) {
                        handleFirstFileWrite(taskInfo, callback);
                    }
                    bos.write(buffer, 0, len);
                    currentSize += len;
                    taskInfo.setCurrentSize(currentSize);
                    int progress = (int) ((currentSize * 100.0 / totalSize) + 0.5);
                    if (progress != oldProgress) {
                        currentRetryTimes = 0;
                        taskInfo.setProgress(progress);
                        handleDownloading(taskInfo, callback);
                        oldProgress = progress;
                    }
                    if (pause) {
                        break;
                    }
                    if (delete) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                isException = true;
                if (!pause && !delete) {
                    retry();
                }
            } finally {
                Utils.close(bos);
                Utils.close(response);
            }
            if (taskInfo.getCurrentSize() == taskInfo.getTotalSize()) {
                handleSuccess(taskInfo, callback);
            } else if (!isException && (!pause && !delete)) {
                //TODO 下载的文件长度有误
            }
        }


        private boolean retry() {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
            if (pause || delete) {
                return false;
            }
            if (currentRetryTimes >= RETRY_MAX_TIMES) {
                //TODO 处理请求失败
                handleFailure(taskInfo, callback);
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                return false;
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
                        if (pause || delete) {
                            timer.cancel();
                            timer = null;
                            return;
                        }
                        HttpCall call = getCall(taskInfo.getResKey(), taskInfo.getUrl(), taskInfo.getCurrentSize());
                        call.enqueue(HttpCallbackImpl.this);
                        timer.cancel();
                        timer = null;
                    }
                    currentRetryTimes++;

                    if (pause || delete) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                    }
                }
            }, RETRYDELAY);
            return true;
        }

        private boolean isWifiOk(Context context) {
            int count = 0;
            while (true) {
                if (pause || delete) {
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

        void setCallback(Callback callback) {
            this.callback = callback;
        }

        void setPause(boolean pause) {
            this.pause = pause;
        }

        void setDelete(boolean delete) {
            this.delete = delete;
        }
    }

}
