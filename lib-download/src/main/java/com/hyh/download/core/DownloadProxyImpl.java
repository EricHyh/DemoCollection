package com.hyh.download.core;

import android.content.Context;
import android.os.RemoteException;

import com.hyh.download.DownloadInfo;
import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.TaskListener;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.ntv.NativeHttpClient;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public class DownloadProxyImpl implements IDownloadProxy {

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
                Thread thread = new Thread(r, "DownloadProxyImpl");
                thread.setDaemon(true);
                return thread;
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
        mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    private final Context mContext;

    private final HttpClient mClient;

    private final TaskHandlerManager mTaskHandlerManager = new TaskHandlerManager();

    private final DownloadProxyConfig mDownloadProxyConfig;

    private final InnerTaskListener mInnerTaskListener = new InnerTaskListenerImpl();

    private final TaskListener mTaskListener;

    private final IFileChecker mGlobalFileChecker;

    private final RequestChecker mRequestChecker;

    private final Object mTaskLock = new Object();

    public DownloadProxyImpl(Context context,
                             DownloadProxyConfig downloadProxyConfig,
                             TaskListener taskListener,
                             IFileChecker globalFileChecker) {
        this.mContext = context;
        this.mClient = new NativeHttpClient(context);
        this.mDownloadProxyConfig = downloadProxyConfig;
        this.mTaskListener = taskListener;
        this.mGlobalFileChecker = globalFileChecker;
        this.mRequestChecker = new RequestChecker(mContext, this);
        TaskDatabaseHelper.getInstance().init(context);
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
    public void startTask(RequestInfo requestInfo, IFileChecker fileChecker) {
        TaskInfo taskInfo = mRequestChecker.check(requestInfo);
        startTask(taskInfo, fileChecker);
    }

    private void startTask(TaskInfo taskInfo, IFileChecker fileChecker) {
        synchronized (mTaskLock) {
            TaskHandler taskHandler = new TaskHandler(mContext,
                    mClient,
                    taskInfo,
                    getFileChecker(fileChecker),
                    mInnerTaskListener,
                    mTaskListener,
                    mDownloadProxyConfig.getThreadMode());

            mTaskHandlerManager.addTask(taskHandler);

            if (!taskHandler.prepare()) return;

            int runningTaskNum = mTaskHandlerManager.getRunningTaskNum();
            if (runningTaskNum >= mDownloadProxyConfig.getMaxSyncDownloadNum()) {
                mTaskHandlerManager.addWaitingTask(taskHandler);
                taskHandler.waitingStart();
                return;
            }

            mTaskHandlerManager.addRunningTask(taskInfo.getResKey());
            taskHandler.run();
        }
    }

    private IFileChecker getFileChecker(IFileChecker fileChecker) {
        if (fileChecker == null && mGlobalFileChecker == null) {
            return null;
        }
        if (fileChecker != null) {
            return fileChecker;
        }
        return mGlobalFileChecker;
    }

    @Override
    public void pauseTask(final String resKey) {
        synchronized (mTaskLock) {
            TaskHandler taskHandler = mTaskHandlerManager.getTask(resKey);
            if (taskHandler != null) {
                taskHandler.pause();
            }
        }
    }

    @Override
    public void deleteTask(final String resKey) {
        synchronized (mTaskLock) {
            TaskHandler taskHandler = mTaskHandlerManager.getTask(resKey);
            if (taskHandler != null) {
                taskHandler.delete();
            } else {
                TaskDatabaseHelper.getInstance().delete(resKey);
            }
        }
    }

    @Override
    public boolean isTaskAlive(String resKey) {
        synchronized (mTaskLock) {
            return mTaskHandlerManager.isTaskAlive(resKey);
        }
    }

    @Override
    public boolean isFileDownloaded(String resKey, IFileChecker fileChecker) {
        synchronized (mTaskLock) {
            boolean isFileDownloaded = false;
            TaskInfo taskInfo = TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
            if (taskInfo != null && taskInfo.getCurrentStatus() == State.SUCCESS) {
                String filePath = DownloadFileHelper.getTaskFilePath(taskInfo);
                long totalSize = taskInfo.getTotalSize();
                if (totalSize <= 0) {
                    isFileDownloaded = DownloadFileHelper.getFileLength(filePath) > 0;
                } else {
                    isFileDownloaded = DownloadFileHelper.getFileLength(filePath) == totalSize;
                }
                if (isFileDownloaded) {
                    if (!checkSuccessFile(taskInfo, fileChecker)) {
                        isFileDownloaded = false;
                    }
                }
                if (!isFileDownloaded) {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
                }
            }
            return isFileDownloaded;
        }
    }

    boolean isFileDownloadedWithoutCheck(String resKey) {
        synchronized (mTaskLock) {
            boolean isFileDownloaded = false;
            TaskInfo taskInfo = TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
            if (taskInfo != null && taskInfo.getCurrentStatus() == State.SUCCESS) {
                String filePath = DownloadFileHelper.getTaskFilePath(taskInfo);
                long totalSize = taskInfo.getTotalSize();
                if (totalSize <= 0) {
                    isFileDownloaded = DownloadFileHelper.getFileLength(filePath) > 0;
                } else {
                    isFileDownloaded = DownloadFileHelper.getFileLength(filePath) == totalSize;
                }
                if (!isFileDownloaded) {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
                }
            }
            return isFileDownloaded;
        }
    }

    @Override
    public DownloadInfo getDownloadInfoByKey(String resKey) {
        TaskInfo taskInfo = getTaskInfoByKey(resKey);
        if (taskInfo != null) {
            return taskInfo.toDownloadInfo();
        }
        return null;
    }

    private boolean checkSuccessFile(TaskInfo taskInfo, IFileChecker fileChecker) {
        fileChecker = getFileChecker(fileChecker);
        if (fileChecker != null) {
            try {
                return fileChecker.isValidFile(taskInfo.toDownloadInfo());
            } catch (RemoteException e) {
                return true;
            }
        }
        return true;
    }

    TaskInfo getTaskInfoByKey(String resKey) {
        synchronized (mTaskLock) {
            return TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
        }
    }

    private void startNextTask() {
        L.d("startNextTask execute run");
        if (mTaskHandlerManager.getRunningTaskNum() > mDownloadProxyConfig.getMaxSyncDownloadNum()) {
            L.d("running task is " + mTaskHandlerManager.getRunningTaskNum());
            return;
        }
        synchronized (mTaskLock) {
            if (mTaskHandlerManager.getRunningTaskNum() > mDownloadProxyConfig.getMaxSyncDownloadNum()) {
                L.d("running task is " + mTaskHandlerManager.getRunningTaskNum());
                return;
            }
            TaskHandler taskHandler = mTaskHandlerManager.pollTask();
            if (taskHandler != null) {
                L.d("startNextTask resKey is " + taskHandler.getResKey());
                mTaskHandlerManager.addRunningTask(taskHandler.getResKey());
                taskHandler.waitingEnd();
                taskHandler.run();
            } else {
                if (mTaskHandlerManager.getRunningTaskNum() == 0) {
                    L.d("startNextTask: 没任务了");
                }
            }
        }
    }

    private class InnerTaskListenerImpl implements InnerTaskListener {

        private List<String> mDownloadingTasks = new CopyOnWriteArrayList<>();

        @Override
        public void onPrepare(TaskInfo taskInfo) {
            handleDatabase(taskInfo);
        }

        @Override
        public void onWaitingInQueue(TaskInfo taskInfo) {
            handleDatabase(taskInfo);
        }

        @Override
        public void onConnected(TaskInfo taskInfo) {
            handleDatabase(taskInfo);
        }

        @Override
        public void onDownloading(TaskInfo taskInfo) {
            handleDatabase(taskInfo);
        }

        @Override
        public void onRetrying(TaskInfo taskInfo) {
            handleDatabase(taskInfo);
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            mTaskHandlerManager.removeTask(taskInfo.getResKey());
            handleDatabase(taskInfo);
            startNextTask();
        }

        @Override
        public void onDelete(TaskInfo taskInfo) {
            mTaskHandlerManager.removeTask(taskInfo.getResKey());
            handleDatabase(taskInfo);
            startNextTask();
        }

        @Override
        public void onSuccess(TaskInfo taskInfo) {
            mTaskHandlerManager.removeTask(taskInfo.getResKey());
            handleDatabase(taskInfo);
            startNextTask();
        }

        @Override
        public void onFailure(TaskInfo taskInfo) {
            mTaskHandlerManager.removeTask(taskInfo.getResKey());
            handleDatabase(taskInfo);
            startNextTask();
        }

        private void handleDatabase(TaskInfo taskInfo) {
            String resKey = taskInfo.getResKey();
            int currentStatus = taskInfo.getCurrentStatus();
            if (currentStatus == State.DOWNLOADING) {
                if (mDownloadingTasks.contains(resKey)) {
                    TaskDatabaseHelper.getInstance().updateCacheOnly(taskInfo);
                } else {
                    TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
                }
                mDownloadingTasks.add(resKey);
            } else if (currentStatus == State.DELETE) {
                mDownloadingTasks.remove(resKey);
                TaskDatabaseHelper.getInstance().delete(taskInfo);
            } else {
                mDownloadingTasks.remove(resKey);
                TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
            }
        }
    }

    private static class TaskHandlerManager {

        private Map<String, TaskHandler> mAliveTaskMap = new ConcurrentHashMap<>();

        private PriorityBlockingQueue<TaskHandler> mWaitingTasks = new PriorityBlockingQueue<>();

        private List<String> mRunningTasks = new CopyOnWriteArrayList<>();

        void addTask(TaskHandler taskHandler) {
            mAliveTaskMap.put(taskHandler.getResKey(), taskHandler);
        }

        void addRunningTask(String resKey) {
            if (!mRunningTasks.contains(resKey)) {
                mRunningTasks.add(resKey);
            }
        }

        int getRunningTaskNum() {
            return mRunningTasks.size();
        }

        TaskHandler pollTask() {
            if (mWaitingTasks.isEmpty()) {
                return null;
            }
            return mWaitingTasks.poll();
        }

        void addWaitingTask(TaskHandler taskHandler) {
            if (!mWaitingTasks.contains(taskHandler)) {
                mWaitingTasks.add(taskHandler);
            }
        }

        TaskHandler getTask(String resKey) {
            return mAliveTaskMap.get(resKey);
        }

        void removeTask(String resKey) {
            mAliveTaskMap.remove(resKey);
            mRunningTasks.remove(resKey);
            mWaitingTasks.remove(new TaskHandler(resKey));
        }

        boolean isTaskAlive(String resKey) {
            return mAliveTaskMap.containsKey(resKey);
        }
    }
}
