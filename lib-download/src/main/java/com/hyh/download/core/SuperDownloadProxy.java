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
import com.hyh.download.utils.RangeUtil;

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

    private Context mContext;

    private HttpClient mClient;

    private final AliveTaskManager mAliveTaskManager = new AliveTaskManager();

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
            int runningTaskNum = mAliveTaskManager.getRunningTaskNum();

            handlePrepare(taskInfo, fileChecker);

            if (runningTaskNum >= mDownloadProxyConfig.getMaxSyncDownloadNum()) {
                handleWaitingInQueue(taskInfo, fileChecker);
                return;
            }

            mAliveTaskManager.addRunningTask(resKey);

            HttpCall httpCall;
            AbstractHttpCallback httpCallback;

            boolean byMultiThread = taskInfo.isByMultiThread();
            int rangeNum = taskInfo.getRangeNum();
            if (!byMultiThread) {
                String filePath = taskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                taskInfo.setCurrentSize(fileLength);
                taskInfo.setProgress(RangeUtil.computeProgress(fileLength, taskInfo.getTotalSize()));
                httpCall = mClient.newCall(resKey, taskInfo.getRequestUrl(), taskInfo.getCurrentSize());
                httpCallback = new SingleHttpCallbackImpl(mContext, mClient, taskInfo, mDownloadCallbackImpl);
            } else {
                if (rangeNum == 1) {
                    String filePath = taskInfo.getFilePath();
                    long fileLength = DownloadFileHelper.getFileLength(filePath);
                    taskInfo.setCurrentSize(fileLength);
                    taskInfo.setProgress(RangeUtil.computeProgress(fileLength, taskInfo.getTotalSize()));
                    httpCall = mClient.newCall(resKey, taskInfo.getRequestUrl(), taskInfo.getCurrentSize());
                    httpCallback = new SingleHttpCallbackImpl(mContext, mClient, taskInfo, mDownloadCallbackImpl);
                } else {
                    httpCall = mClient.newCall(resKey, taskInfo.getRequestUrl(), -1);
                    httpCallback = new MultiHttpCallbackImpl(mContext, mClient, taskInfo, mDownloadCallbackImpl);
                }
            }
            mAliveTaskManager.putHttpCallback(resKey, httpCallback);
            httpCall.enqueue(httpCallback);
        }
    }

    @Override
    public void pauseTask(final String resKey) {
        synchronized (mTaskLock) {
            AbstractHttpCallback callback = mAliveTaskManager.removeHttpCallback(resKey);
            if (callback != null) {
                callback.cancel();
            }
            TaskWrapper taskWrapper = mAliveTaskManager.removeTask(resKey);
            handlePause(taskWrapper.taskInfo);
        }
    }

    @Override
    public void deleteTask(final String resKey) {
        synchronized (mTaskLock) {
            TaskInfo taskInfo;
            AbstractHttpCallback callback = mAliveTaskManager.removeHttpCallback(resKey);
            if (callback != null) {
                callback.cancel();
            }
            TaskWrapper taskWrapper = mAliveTaskManager.removeTask(resKey);
            if (taskWrapper != null) {
                taskInfo = taskWrapper.taskInfo;
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
            return mAliveTaskManager.isTaskAlive(resKey);
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
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
                }
            }
            return isFileDownloaded;
        }
    }

    @Override
    public boolean isFileDownloaded(String resKey, int versionCode, IFileChecker fileChecker) {
        synchronized (mTaskLock) {
            boolean isFileDownloaded = false;
            TaskInfo taskInfo = TaskDatabaseHelper.getInstance().getTaskInfoByKey(resKey);
            if (taskInfo != null && taskInfo.getCurrentStatus() == State.SUCCESS && taskInfo.getVersionCode() == versionCode) {
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
            L.d("startNextTask execute start");
            TaskWrapper taskWrapper = mAliveTaskManager.pollNextTask();
            if (taskWrapper != null) {
                startTask(taskWrapper.taskInfo, taskWrapper.fileChecker);
                L.d("startNextTask resKey is " + taskWrapper.resKey);
            } else {
                if (mAliveTaskManager.getRunningTaskNum() == 0) {
                    L.d("startNextTask: 没任务了");
                }
            }
            L.d("startNextTask execute end");
        }
    }

    private void handlePrepare(TaskInfo taskInfo, IFileChecker fileChecker) {
        String resKey = taskInfo.getResKey();
        mAliveTaskManager.addTask(new TaskWrapper(resKey, taskInfo, fileChecker));

        taskInfo.setCurrentStatus(State.PREPARE);

        handleDatabase(taskInfo);

        handleCallback(taskInfo);
    }


    private void handleWaitingInQueue(TaskInfo taskInfo, IFileChecker fileChecker) {
        String resKey = taskInfo.getResKey();
        taskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);

        mAliveTaskManager.addWaitingTask(new TaskWrapper(resKey, taskInfo, fileChecker));

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

        mAliveTaskManager.removeTask(resKey);

        taskInfo.setCurrentStatus(State.PAUSE);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);

        startNextTask();
    }

    private void handleDelete(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();

        mAliveTaskManager.removeTask(resKey);

        DownloadFileHelper.deleteDownloadFile(taskInfo);
        taskInfo.setCurrentStatus(State.DELETE);
        handleCallback(taskInfo);
        handleDatabase(taskInfo);

        startNextTask();
    }


    private void handleSuccess(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        if (!isTaskAlive(resKey)) return;

        TaskWrapper taskWrapper = mAliveTaskManager.getTaskWrapper(resKey);
        boolean isSuccess = true;
        if (!checkFile(taskInfo, taskWrapper.fileChecker)) {
            isSuccess = false;

            DownloadFileHelper.deleteDownloadFile(taskInfo);
            TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);

            if (taskInfo.isPermitRetryInvalidFileTask()) {

                mAliveTaskManager.removeTask(resKey);

                startTask(taskInfo, taskWrapper.fileChecker);
            } else {
                handleFailure(taskInfo);
            }
        }

        if (isSuccess) {
            mAliveTaskManager.removeTask(resKey);

            taskInfo.setCurrentStatus(State.SUCCESS);
            handleCallback(taskInfo);
            handleDatabase(taskInfo);
            startNextTask();
        }
    }

    private void handleFailure(TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        if (!isTaskAlive(resKey)) return;

        mAliveTaskManager.removeTask(resKey);

        if (taskInfo.isWifiAutoRetry()) {
            taskInfo.setCurrentStatus(State.WAITING_FOR_WIFI);
        } else {
            taskInfo.setCurrentStatus(State.FAILURE);
        }

        handleCallback(taskInfo);
        handleDatabase(taskInfo);

        startNextTask();
    }

    protected abstract void handleCallback(TaskInfo taskInfo);

    private List<String> mDownloadingTasks = new CopyOnWriteArrayList<>();

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

    private class DownloadCallbackImpl implements DownloadCallback {

        @Override
        public void onConnected(TaskInfo taskInfo, Map<String, List<String>> responseHeaderFields) {

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

    private static class TaskWrapper implements Comparable {

        private String resKey;

        private TaskInfo taskInfo;

        private IFileChecker fileChecker;

        TaskWrapper(String resKey) {
            this.resKey = resKey;
        }

        TaskWrapper(String resKey, TaskInfo taskInfo, IFileChecker fileChecker) {
            this.resKey = resKey;
            this.taskInfo = taskInfo;
            this.fileChecker = fileChecker;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            TaskWrapper that = (TaskWrapper) object;
            return resKey.equals(that.resKey);
        }

        @Override
        public int compareTo(Object o) {
            if (o == null || !(o instanceof TaskWrapper)) {
                return 1;
            }
            if (this.taskInfo == null) {
                return 1;
            }
            TaskWrapper that = (TaskWrapper) o;
            if (that.taskInfo == null) {
                return 1;
            }
            if (this.taskInfo.getPriority() > that.taskInfo.getPriority()) {
                return -1;
            }
            return 1;
        }
    }

    private static class AliveTaskManager {

        private Map<String, TaskWrapper> mAliveTaskMap = new ConcurrentHashMap<>();

        private PriorityBlockingQueue<TaskWrapper> mWaitingTasks = new PriorityBlockingQueue<>();

        private Map<String, AbstractHttpCallback> mHttpCallbackMap = new ConcurrentHashMap<>();

        private List<String> mRunningTasks = new CopyOnWriteArrayList<>();

        void addRunningTask(String resKey) {
            if (!mRunningTasks.contains(resKey)) {
                mRunningTasks.add(resKey);
            }
        }

        int getRunningTaskNum() {
            return mRunningTasks.size();
        }

        TaskWrapper pollNextTask() {
            if (mWaitingTasks.isEmpty()) {
                return null;
            }
            return mWaitingTasks.poll();
        }

        void addTask(TaskWrapper taskWrapper) {
            mAliveTaskMap.put(taskWrapper.resKey, taskWrapper);
        }

        void addWaitingTask(TaskWrapper taskWrapper) {
            if (!mWaitingTasks.contains(taskWrapper)) {
                mWaitingTasks.add(taskWrapper);
            }
        }

        void putHttpCallback(String resKey, AbstractHttpCallback httpCallback) {
            mHttpCallbackMap.put(resKey, httpCallback);
        }

        AbstractHttpCallback removeHttpCallback(String resKey) {
            return mHttpCallbackMap.remove(resKey);
        }

        TaskWrapper removeTask(String resKey) {
            TaskWrapper taskWrapper = mAliveTaskMap.remove(resKey);
            mRunningTasks.remove(resKey);
            mWaitingTasks.remove(new TaskWrapper(resKey));
            mHttpCallbackMap.remove(resKey);
            return taskWrapper;
        }

        boolean isTaskAlive(String resKey) {
            return mAliveTaskMap.containsKey(resKey);
        }

        TaskWrapper getTaskWrapper(String resKey) {
            return mAliveTaskMap.get(resKey);
        }
    }
}
