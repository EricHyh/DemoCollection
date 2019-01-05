package com.hyh.download.core;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.ThreadMode;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.RangeUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Eric_He on 2019/1/4.
 */

public class TaskHandler implements Comparable<TaskHandler> {

    private final Object mLock = new Object();

    private Context mContext;

    private HttpClient mClient;

    private String mResKey;

    private TaskInfo mTaskInfo;

    private IFileChecker mGlobalFileChecker;

    private IFileChecker mFileChecker;

    private RunningTaskStopListener mRunningTaskStopListener;

    private int mThreadMode;

    private Handler mPostHandler;

    private AbstractHttpCallback mHttpCallback;

    private DownloadCallbackImpl mDownloadCallback = new DownloadCallbackImpl();

    private volatile boolean mIsPrepared;

    private volatile boolean mIsRetryInvalidFileTask;

    public TaskHandler(String resKey) {
        mResKey = resKey;
    }

    public TaskHandler(Context context,
                       HttpClient client,
                       TaskInfo taskInfo,
                       IFileChecker globalFileChecker,
                       IFileChecker fileChecker,
                       RunningTaskStopListener listener,
                       int threadMode) {
        mContext = context;
        mClient = client;
        mTaskInfo = taskInfo;
        mGlobalFileChecker = globalFileChecker;
        mFileChecker = fileChecker;
        mRunningTaskStopListener = listener;
        mThreadMode = threadMode;

        if (mThreadMode == ThreadMode.UI) {
            mPostHandler = new Handler(Looper.getMainLooper(), new HandlerCallback());
        } else if (mThreadMode == ThreadMode.BACKGROUND) {
            HandlerThread handlerThread = new HandlerThread("TaskHandler-" + mResKey.hashCode());
            handlerThread.start();
            mPostHandler = new Handler(handlerThread.getLooper(), new HandlerCallback());
        }
    }

    public String getResKey() {
        return mResKey;
    }

    public void prepare() {
        synchronized (mLock) {
            if (mPostHandler != null) {
                mPostHandler.sendMessage(mPostHandler.obtainMessage(State.PREPARE));
            } else {
                handlePrepare();
            }
        }
    }

    public void waiting() {
        synchronized (mLock) {
            handleWaitingInQueue();
        }
    }

    public void start() {
        synchronized (mLock) {

            HttpCall httpCall;
            AbstractHttpCallback httpCallback;

            boolean byMultiThread = mTaskInfo.isByMultiThread();
            int rangeNum = mTaskInfo.getRangeNum();
            if (!byMultiThread) {
                String filePath = mTaskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                mTaskInfo.setCurrentSize(fileLength);
                mTaskInfo.setProgress(RangeUtil.computeProgress(fileLength, mTaskInfo.getTotalSize()));
                httpCall = mClient.newCall(mResKey, mTaskInfo.getRequestUrl(), mTaskInfo.getCurrentSize());
                httpCallback = new SingleHttpCallbackImpl(mContext, mClient, mTaskInfo, mDownloadCallback);
            } else {
                if (rangeNum == 1) {
                    String filePath = mTaskInfo.getFilePath();
                    long fileLength = DownloadFileHelper.getFileLength(filePath);
                    mTaskInfo.setCurrentSize(fileLength);
                    mTaskInfo.setProgress(RangeUtil.computeProgress(fileLength, mTaskInfo.getTotalSize()));
                    httpCall = mClient.newCall(mResKey, mTaskInfo.getRequestUrl(), mTaskInfo.getCurrentSize());
                    httpCallback = new SingleHttpCallbackImpl(mContext, mClient, mTaskInfo, mDownloadCallback);
                } else {
                    httpCall = mClient.newCall(mResKey, mTaskInfo.getRequestUrl(), -1);
                    httpCallback = new MultiHttpCallbackImpl(mContext, mClient, mTaskInfo, mDownloadCallback);
                }
            }
            mHttpCallback = httpCallback;
            httpCall.enqueue(httpCallback);
        }
    }

    public void pause() {
        synchronized (mLock) {
            final AbstractHttpCallback callback = mHttpCallback;
            if (callback != null) {
                callback.cancel();
            }
            handlePause();
        }
    }

    public void delete() {
        synchronized (mLock) {
            AbstractHttpCallback callback = mHttpCallback;
            if (callback != null) {
                callback.cancel();
            }
            handleDelete();
        }
    }


    private void handlePrepare() {

    }

    private void handleWaitingInQueue() {

    }

    private void handleConnected(Map<String, List<String>> responseHeaderFields) {

    }

    private void handleDownloading() {

    }

    private void handlePause() {

    }

    private void handleDelete() {

    }

    private void handleFailure() {

    }

    private void handleSuccess() {
        synchronized (mLock) {
            if (!mIsPrepared) return;
        }


        boolean isSuccess = true;
        if (!checkFile()) {
            isSuccess = false;

            DownloadFileHelper.deleteDownloadFile(mTaskInfo);
            TaskDatabaseHelper.getInstance().insertOrUpdate(mTaskInfo);

            if (!mIsRetryInvalidFileTask && mTaskInfo.isPermitRetryInvalidFileTask()) {
                mIsRetryInvalidFileTask = true;
                /*mAliveTaskManager.removeTask(resKey);
                startTask(taskInfo, taskWrapper.fileChecker);*/
                start();

            } else {
                handleFailure();
            }
        }

        if (isSuccess) {

            mTaskInfo.setCurrentStatus(State.SUCCESS);

            if (mRunningTaskStopListener != null) {
                mRunningTaskStopListener.onFinish(mTaskInfo);
            }

            /*handleCallback(taskInfo);
            handleDatabase(taskInfo);
            startNextTask();*/
        }
    }


    private boolean checkFile() {
        if (mFileChecker != null) {
            try {
                return mFileChecker.isValidFile(mTaskInfo.toDownloadInfo());
            } catch (RemoteException e) {
                //
            }
        }
        if (mGlobalFileChecker != null) {
            try {
                return mGlobalFileChecker.isValidFile(mTaskInfo.toDownloadInfo());
            } catch (RemoteException e) {
                //
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TaskHandler that = (TaskHandler) object;
        return mResKey.equals(that.mResKey);
    }

    @Override
    public int compareTo(TaskHandler taskHandler) {
        if (taskHandler == null) {
            return 1;
        }
        if (this.mTaskInfo == null) {
            return 1;
        }
        TaskHandler that = taskHandler;
        if (that.mTaskInfo == null) {
            return 1;
        }
        if (this.mTaskInfo.getPriority() > that.mTaskInfo.getPriority()) {
            return -1;
        }
        return 1;
    }

    private class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            int state = msg.what;
            switch (state) {
                case State.PREPARE: {
                    break;
                }
                case State.WAITING_IN_QUEUE: {
                    break;
                }
                case State.CONNECTED: {
                    break;
                }
                case State.DOWNLOADING: {
                    break;
                }
                case State.PAUSE: {
                    break;
                }
                case State.DELETE: {
                    break;
                }
                case State.SUCCESS: {
                    break;
                }
                case State.FAILURE: {
                    break;
                }
            }
            return true;
        }
    }

    private class DownloadCallbackImpl implements DownloadCallback {

        @Override
        public void onConnected(TaskInfo taskInfo, Map<String, List<String>> responseHeaderFields) {
            handleConnected(responseHeaderFields);
        }

        @Override
        public void onDownloading(TaskInfo taskInfo) {
            handleDownloading();
        }

        @Override
        public void onFailure(TaskInfo taskInfo) {
            handleFailure();
        }

        @Override
        public void onSuccess(TaskInfo taskInfo) {
            handleSuccess();
        }
    }

    public interface RunningTaskStopListener {

        void onFinish(TaskInfo taskInfo);

    }
}
