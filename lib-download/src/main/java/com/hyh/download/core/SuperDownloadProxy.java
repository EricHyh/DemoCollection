package com.hyh.download.core;

import android.content.Context;
import android.os.RemoteException;

import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.ntv.NativeHttpClient;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
                Thread thread = new Thread(r, "SuperDownloadProxy");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    protected Context mContext;

    private HttpClient mClient;

    private HttpCallFactory mCallFactory = new HttpCallFactory(mExecutor);

    private Map<String, TaskWrapper> mAliveTaskMap = new ConcurrentHashMap<>();

    private Map<String, AbstractHttpCallback> mHttpCallbackMap = new ConcurrentHashMap<>();

    private Map<String, TaskCache> mWaitingQueue = new ConcurrentHashMap<>();

    private DownloadProxyConfig mDownloadProxyConfig;

    private IFileChecker mGlobalFileChecker;

    private DownloadCallbackImpl mDownloadCallbackImpl;

    private final Object mTaskLock = new Object();

    SuperDownloadProxy(Context context, DownloadProxyConfig downloadProxyConfig, IFileChecker globalFileChecker) {
        this.mContext = context;
        this.mClient = new NativeHttpClient(context);
        this.mDownloadProxyConfig = downloadProxyConfig;
        this.mGlobalFileChecker = globalFileChecker;
        this.mDownloadCallbackImpl = new DownloadCallbackImpl();
        TaskDatabaseHelper.getInstance().init(context);
    }

    private AbstractHttpCallback getHttpCallbackImpl(TaskInfo taskInfo) {
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum > 1) {
            return new MultiHttpCallbackImpl(mContext, mClient, taskInfo, mDownloadCallbackImpl);
        } else {
            return new SingleHttpCallbackImpl(mContext, mClient, taskInfo, mDownloadCallbackImpl);
        }
    }

    @Override
    public void initProxy(final Runnable afterInit) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<TaskInfo> interruptedTasks = TaskDatabaseHelper
                        .getInstance()
                        .getInterruptedTasks();
                if (interruptedTasks != null && !interruptedTasks.isEmpty()) {
                    for (TaskInfo interruptedTask : interruptedTasks) {
                        startTask(interruptedTask, null);
                    }
                }
                afterInit.run();
            }
        });
    }

    @Override
    public void insertOrUpdate(TaskInfo taskInfo) {
        TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
    }

    @Override
    public void startTask(final TaskInfo taskInfo, final IFileChecker fileChecker) {
        synchronized (mTaskLock) {
            final String resKey = taskInfo.getResKey();
            int runningTaskSize = mHttpCallbackMap.size();

            handlePrepare(taskInfo, fileChecker);

            if (mCallFactory.isCreating(taskInfo.getResKey())) return;

            if (runningTaskSize >= mDownloadProxyConfig.getMaxSyncDownloadNum()) {
                handleWaitingInQueue(taskInfo, fileChecker);
                return;
            }

            mCallFactory.create(mClient, taskInfo, new HttpCallFactory.HttpCallCreateListener() {
                @Override
                public void onCreateFinish(HttpCall call) {
                    if (!isTaskAlive(resKey)) {
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
                    mHttpCallbackMap.put(resKey, httpCallbackImpl);
                    call.enqueue(httpCallbackImpl);
                }
            });
        }
    }

    @Override
    public void pauseTask(final String resKey) {
        synchronized (mTaskLock) {
            TaskWrapper taskWrapper = mAliveTaskMap.remove(resKey);

            mWaitingQueue.remove(resKey);
            AbstractHttpCallback remove = mHttpCallbackMap.remove(resKey);
            if (remove != null) {
                remove.pause();
            }

            handlePause(taskWrapper.taskInfo);

        }
    }

    @Override
    public void deleteTask(final String resKey) {
        synchronized (mTaskLock) {
            TaskWrapper taskWrapper = mAliveTaskMap.remove(resKey);
            TaskInfo taskInfo;
            if (taskWrapper != null) {
                taskInfo = taskWrapper.taskInfo;
                mWaitingQueue.remove(resKey);
                AbstractHttpCallback remove = mHttpCallbackMap.remove(resKey);
                if (remove != null) {
                    remove.pause();
                }
            } else {
                taskInfo = TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
            }
            if (taskInfo != null) {
                handleDelete(taskInfo);
            }
        }
    }

    @Override
    public boolean isTaskAlive(String resKey) {
        synchronized (mTaskLock) {
            return mAliveTaskMap.containsKey(resKey);
        }
    }

    @Override
    public boolean isFileDownloaded(String resKey, IFileChecker fileChecker) {
        synchronized (mTaskLock) {
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
                if (isFileDownloaded) {
                    if (!checkFile(taskInfo, fileChecker)) {
                        isFileDownloaded = false;
                    }
                }
                if (!isFileDownloaded) {
                    taskInfo.setProgress(0);
                    taskInfo.setCurrentSize(0);
                    taskInfo.setTotalSize(0);
                    taskInfo.setCurrentStatus(State.NONE);
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
                }
            }
            return isFileDownloaded;
        }
    }

    private boolean checkFile(TaskInfo taskInfo, IFileChecker fileChecker) {
        if (fileChecker != null) {
            try {
                return fileChecker.isValidFile(taskInfo.toDownloadInfo());
            } catch (RemoteException e) {
                //
            }
        }
        if (mGlobalFileChecker != null) {
            try {
                return mGlobalFileChecker.isValidFile(taskInfo.toDownloadInfo());
            } catch (RemoteException e) {
                //
            }
        }
        return true;
    }

    @Override
    public TaskInfo getTaskInfoByKey(String resKey) {
        synchronized (mTaskLock) {
            return TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
        }
    }

    private void startNextTask() {
        synchronized (mTaskLock) {
            L.d("startNextTask start");
            Iterator<Map.Entry<String, TaskCache>> iterator = mWaitingQueue.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, TaskCache> entry = iterator.next();
                String resKey = entry.getKey();
                TaskCache taskCache = entry.getValue();
                iterator.remove();
                L.d("startNextTask resKey is " + resKey);
                startTask(taskCache.taskInfo, taskCache.fileChecker);
            } else {
                if (mHttpCallbackMap.isEmpty()) {
                    handleHaveNoTask();
                    L.d("startNextTask: 没任务了");
                }
            }
        }
    }

    private void handlePrepare(TaskInfo taskInfo, IFileChecker fileChecker) {
        String resKey = taskInfo.getResKey();
        mAliveTaskMap.put(resKey, new TaskWrapper(taskInfo, fileChecker));

        taskInfo.setCurrentStatus(State.PREPARE);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }


    private void handleWaitingInQueue(TaskInfo taskInfo, IFileChecker fileChecker) {
        String resKey = taskInfo.getResKey();
        taskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);
        TaskCache taskCache = new TaskCache(resKey, taskInfo, fileChecker);
        mWaitingQueue.remove(taskInfo.getResKey());
        mWaitingQueue.put(resKey, taskCache);

        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }

    private void handleDownloading(TaskInfo taskInfo) {
        if (!isTaskAlive(taskInfo.getResKey())) return;

        taskInfo.setCurrentStatus(State.DOWNLOADING);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);
    }


    private void handlePause(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mAliveTaskMap.remove(resKey);

        taskInfo.setCurrentStatus(State.PAUSE);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);

        startNextTask();
    }

    private void handleDelete(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        mAliveTaskMap.remove(resKey);

        DownloadFileHelper.deleteDownloadFile(taskInfo);
        taskInfo.setCurrentStatus(State.DELETE);
        taskInfo.setProgress(0);
        taskInfo.setCurrentSize(0);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);

        startNextTask();
    }


    private void handleSuccess(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        if (!isTaskAlive(resKey)) return;

        TaskWrapper taskWrapper = mAliveTaskMap.get(resKey);
        boolean isSuccess = true;
        if (!checkFile(taskInfo, taskWrapper.fileChecker)) {
            isSuccess = false;
            taskInfo.setProgress(0);
            taskInfo.setCurrentSize(0);
            taskInfo.setTotalSize(0);
            taskInfo.setCurrentStatus(State.NONE);
            DownloadFileHelper.deleteDownloadFile(taskInfo);
            TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);

            if (taskInfo.isPermitRetryInvalidFileTask()) {
                mAliveTaskMap.remove(resKey);
                startTask(taskInfo, taskWrapper.fileChecker);
            } else {
                handleFailure(taskInfo);
            }
        }

        if (isSuccess) {
            taskInfo.setCurrentStatus(State.SUCCESS);
            mHttpCallbackMap.remove(resKey);
            handleCallback(taskInfo);
            handleDatabase(taskInfo);
            startNextTask();
        }
    }

    private void handleFailure(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        if (!isTaskAlive(resKey)) return;

        mAliveTaskMap.remove(resKey);

        if (taskInfo.isWifiAutoRetry()) {
            taskInfo.setCurrentStatus(State.WAITING_FOR_WIFI);
        } else {
            taskInfo.setCurrentStatus(State.FAILURE);
        }
        mHttpCallbackMap.remove(resKey);

        handleDatabase(taskInfo);
        handleCallback(taskInfo);

        startNextTask();
    }

    protected abstract void handleHaveNoTask();

    protected abstract void handleCallback(TaskInfo taskInfo);

    private List<String> mDownloadingStatusTasks = new CopyOnWriteArrayList<>();

    private void handleDatabase(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        int currentStatus = taskInfo.getCurrentStatus();
        if (currentStatus == State.DOWNLOADING) {
            if (mDownloadingStatusTasks.contains(resKey)) {
                TaskDatabaseHelper.getInstance().updateCacheOnly(taskInfo);
            } else {
                TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
            }
            mDownloadingStatusTasks.add(resKey);
        } else if (currentStatus == State.DELETE) {
            mDownloadingStatusTasks.remove(resKey);
            TaskDatabaseHelper.getInstance().delete(taskInfo);
        } else {
            mDownloadingStatusTasks.remove(resKey);
            TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
        }
    }

    private class DownloadCallbackImpl implements DownloadCallback {

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

    private static class TaskWrapper {

        private TaskInfo taskInfo;

        private IFileChecker fileChecker;

        TaskWrapper(TaskInfo taskInfo, IFileChecker fileChecker) {
            this.taskInfo = taskInfo;
            this.fileChecker = fileChecker;
        }
    }
}
