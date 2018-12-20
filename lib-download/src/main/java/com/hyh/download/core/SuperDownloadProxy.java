package com.hyh.download.core;

import android.content.Context;

import com.hyh.download.FileChecker;
import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.ntv.NativeHttpClient;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public abstract class SuperDownloadProxy implements IDownloadProxy {


    private final ThreadPoolExecutor mExecutor;

    {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 120L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "FileDownloader");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    protected Context context;

    private HttpClient client;

    private HttpCallFactory callFactory = new HttpCallFactory();

    private Map<String, AbstractHttpCallback> httpCallbackMap = new ConcurrentHashMap<>();

    private Map<String, TaskCache> waitingQueue = new ConcurrentHashMap<>();

    private int maxSynchronousDownloadNum;

    private DownloadCallbackImpl mDownloadCallbackImpl;

    private Map<String, Integer> mTaskCommandMap = new ConcurrentHashMap<>();

    private final Object mCommandLock = new Object();

    SuperDownloadProxy(Context context, int maxSynchronousDownloadNum) {
        this.context = context;
        this.client = new NativeHttpClient(context);
        this.maxSynchronousDownloadNum = maxSynchronousDownloadNum;
        this.mDownloadCallbackImpl = new DownloadCallbackImpl();
    }

    private AbstractHttpCallback getHttpCallbackImpl(TaskInfo taskInfo) {
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum > 1) {
            return new MultiHttpCallbackImpl(context, client, taskInfo, mDownloadCallbackImpl);
        } else {
            return new SingleHttpCallbackImpl(context, client, taskInfo, mDownloadCallbackImpl);
        }
    }

    @Override
    public void initProxy(final Runnable afterInit) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TaskDatabaseHelper.getInstance().fixDatabaseErrorStatus();
                afterInit.run();
            }
        });
    }

    @Override
    public void insertOrUpdate(TaskInfo taskInfo) {
        TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
    }

    @Override
    public void onReceiveStartCommand(String resKey) {
        synchronized (mCommandLock) {
            mTaskCommandMap.put(resKey, Command.START);
        }
    }

    @Override
    public void onReceivePauseCommand(String resKey) {
        synchronized (mCommandLock) {
            mTaskCommandMap.put(resKey, Command.PAUSE);
        }
    }

    @Override
    public void onReceiveDeleteCommand(String resKey) {
        synchronized (mCommandLock) {
            mTaskCommandMap.put(resKey, Command.DELETE);
        }
    }

    @Override
    public void startTask(final TaskInfo taskInfo, final FileChecker fileChecker) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final String resKey = taskInfo.getResKey();
                if (!checkCommand(resKey, Command.START)) return;
                int runningTaskSize = httpCallbackMap.size();
                AbstractHttpCallback httpCallbackImpl = httpCallbackMap.get(resKey);
                TaskInfo cacheTaskInfo = null;
                if (httpCallbackImpl != null) {
                    cacheTaskInfo = httpCallbackImpl.getTaskInfo();
                }
                if (cacheTaskInfo != null) {//检查状态
                    if (!canDownload(cacheTaskInfo.getCurrentStatus())) {
                        return;
                    }
                }
                if (runningTaskSize >= maxSynchronousDownloadNum) {
                    handleWaitingInQueue(taskInfo, fileChecker);
                    return;
                }
                callFactory.create(client, taskInfo, new HttpCallFactory.HttpCallCreateListener() {
                    @Override
                    public void onCreateFinish(HttpCall call) {
                        if (!checkCommand(resKey, Command.START)) {
                            if (call != null) {
                                call.cancel();
                            }
                            return;
                        }
                        if (call == null) {
                            handleFailure(taskInfo);
                            return;
                        }
                        taskInfo.setCurrentStatus(State.DOWNLOADING);
                        insertOrUpdate(taskInfo);
                        AbstractHttpCallback httpCallbackImpl = getHttpCallbackImpl(taskInfo);
                        httpCallbackMap.put(resKey, httpCallbackImpl);
                        call.enqueue(httpCallbackImpl);
                    }
                });
            }
        });
    }

    @Override
    public void pauseTask(final String resKey) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!checkCommand(resKey, Command.PAUSE)) return;
                AbstractHttpCallback remove = httpCallbackMap.remove(resKey);
                if (remove != null) {
                    remove.pause();
                    handlePause();
                } else {
                    TaskCache taskCache = waitingQueue.remove(resKey);
                    if (taskCache != null) {
                        handlePause();
                    }
                }
            }
        });
    }

    @Override
    public void deleteTask(final String resKey) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!checkCommand(resKey, Command.DELETE)) return;
                AbstractHttpCallback remove = httpCallbackMap.remove(resKey);
                if (remove != null) {
                    remove.delete();
                    handleDelete();
                } else {
                    TaskCache taskCache = waitingQueue.remove(resKey);
                    if (taskCache != null) {
                        waitingQueue.remove(resKey);
                        handleDelete();
                    }
                }
            }
        });
    }

    @Override
    public boolean isTaskAlive(String resKey) {
        synchronized (mCommandLock) {
            Integer integer = mTaskCommandMap.get(resKey);
            if (integer != null && integer != Command.START) {
                return false;
            }
        }
        return waitingQueue.containsKey(resKey) || httpCallbackMap.containsKey(resKey);
    }

    @Override
    public boolean isFileDownloaded(String resKey) {
        synchronized (mCommandLock) {
            Integer integer = mTaskCommandMap.get(resKey);
            if (integer != null && integer == Command.DELETE) {
                return false;
            }
        }
        boolean isFileDownloaded = false;
        TaskInfo taskInfo = TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
        if (taskInfo != null && taskInfo.getCurrentStatus() == State.SUCCESS) {
            String filePath = taskInfo.getFilePath();
            long totalSize = taskInfo.getTotalSize();
            if (totalSize <= 0) {
                isFileDownloaded = DownloadFileHelper.getFileLength(filePath) > 0;
            } else {
                isFileDownloaded = DownloadFileHelper.getFileLength(filePath) == totalSize;
            }
            if (!isFileDownloaded) {
                taskInfo.setProgress(0);
                taskInfo.setCurrentSize(0);
                taskInfo.setTotalSize(0);
                taskInfo.setCurrentStatus(State.NONE);
            }
        }
        return isFileDownloaded;
    }

    @Override
    public TaskInfo getTaskInfoByKey(String resKey) {
        return TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
    }


    private boolean checkCommand(String resKey, int command) {
        synchronized (mCommandLock) {
            Integer integer = mTaskCommandMap.get(resKey);
            return integer != null && integer == command;
        }
    }

    private boolean canDownload(int currentStatus) {
        if (currentStatus == State.PREPARE
                || currentStatus == State.START_WRITE
                || currentStatus == State.DOWNLOADING
                || currentStatus == State.WAITING_IN_QUEUE
                || currentStatus == State.SUCCESS) {
            return false;
        }
        return true;
    }

    private synchronized void startNextTask() {
        L.d("startNextTask start");
        Iterator<Map.Entry<String, TaskCache>> iterator = waitingQueue.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, TaskCache> entry = iterator.next();
            String resKey = entry.getKey();
            TaskCache taskCache = entry.getValue();
            iterator.remove();
            L.d("startNextTask resKey is " + resKey);
            startTask(taskCache.taskInfo, taskCache.fileChecker);
        } else {
            if (httpCallbackMap.isEmpty()) {
                handleHaveNoTask();
                L.d("startNextTask: 没任务了");
            }
        }
    }

    private void handleFirstFileWrite(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.START_WRITE);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }

    private void handlePause() {
        startNextTask();
    }

    private void handleDelete() {
        startNextTask();
    }


    private void handleWaitingInQueue(TaskInfo taskInfo, FileChecker fileChecker) {
        String resKey = taskInfo.getResKey();
        taskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);
        TaskCache taskCache = new TaskCache(resKey, taskInfo, fileChecker);
        waitingQueue.remove(taskInfo.getResKey());
        waitingQueue.put(resKey, taskCache);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }


    private void handleSuccess(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.SUCCESS);
        String resKey = taskInfo.getResKey();
        httpCallbackMap.remove(resKey);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
        startNextTask();
    }

    private void handleFailure(TaskInfo taskInfo) {
        if (taskInfo.isWifiAutoRetry()) {
            taskInfo.setCurrentStatus(State.WAITING_FOR_WIFI);
        } else {
            taskInfo.setCurrentStatus(State.FAILURE);
        }
        String resKey = taskInfo.getResKey();
        httpCallbackMap.remove(resKey);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
        startNextTask();
    }


    private void handleDownloading(TaskInfo taskInfo) {
        taskInfo.setCurrentStatus(State.DOWNLOADING);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }

    protected abstract void handleHaveNoTask();

    protected abstract void handleCallback(TaskInfo taskInfo);

    private void handleDatabase(TaskInfo taskInfo) {
        TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
    }

    private class DownloadCallbackImpl implements DownloadCallback {

        @Override
        public void onFirstFileWrite(TaskInfo taskInfo) {
            handleFirstFileWrite(taskInfo);
        }

        @Override
        public void onDownloading(TaskInfo taskInfo) {
            handleDownloading(taskInfo);
        }

        @Override
        public void onFailure(TaskInfo taskInfo) {
            handleFailure(taskInfo);
        }

        @Override
        public void onSuccess(TaskInfo taskInfo) {
            handleSuccess(taskInfo);
        }
    }
}
