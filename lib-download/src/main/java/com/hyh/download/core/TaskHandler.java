package com.hyh.download.core;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.hyh.download.DownloadInfo;
import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.TaskListener;
import com.hyh.download.ThreadMode;
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

    private final String mResKey;

    private TaskInfo mTaskInfo;

    private IFileChecker mFileChecker;

    private InnerTaskListener mInnerTaskListener;

    private TaskListener mOuterTaskListener;

    private int mThreadMode;

    private Handler mPostHandler;

    private AbstractHttpCallback mHttpCallback;

    private DownloadCallbackImpl mDownloadCallback = new DownloadCallbackImpl();

    private Speed mSpeed = new Speed();

    private volatile boolean mIsPrepared;
    private HandlerThread mNotifyThread;

    TaskHandler(String resKey) {
        mResKey = resKey;
    }

    TaskHandler(Context context,
                HttpClient client,
                TaskInfo taskInfo,
                IFileChecker fileChecker,
                InnerTaskListener innerTaskListener,
                TaskListener outerTaskListener,
                int threadMode) {
        mContext = context;
        mClient = client;
        mResKey = taskInfo.getResKey();
        mTaskInfo = taskInfo;
        mFileChecker = fileChecker;
        mInnerTaskListener = innerTaskListener;
        mOuterTaskListener = outerTaskListener;
        mThreadMode = threadMode;

        if (mThreadMode == ThreadMode.UI) {
            mPostHandler = new Handler(Looper.getMainLooper(), new HandlerCallback());
        } else if (mThreadMode == ThreadMode.BACKGROUND) {
            mNotifyThread = new HandlerThread("TaskHandler-" + mResKey.hashCode());
            mNotifyThread.start();
            mPostHandler = new Handler(mNotifyThread.getLooper(), new HandlerCallback());
        }
    }

    String getResKey() {
        return mResKey;
    }

    void prepare() {
        synchronized (mLock) {
            mIsPrepared = true;

            mTaskInfo.setCurrentStatus(State.PREPARE);

            mInnerTaskListener.onPrepare(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.PREPARE);
                message.obj = downloadInfo;
                sendMessage(message);
            } else {
                notifyPrepare(downloadInfo);
            }
        }
    }

    void waiting() {
        synchronized (mLock) {

            mTaskInfo.setCurrentStatus(State.WAITING_IN_QUEUE);

            mInnerTaskListener.onWaitingInQueue(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.WAITING_IN_QUEUE);
                message.obj = downloadInfo;
                sendMessage(message);
            } else {
                notifyWaitingInQueue(downloadInfo);
            }
        }
    }

    void run() {
        synchronized (mLock) {

            HttpCall httpCall;
            AbstractHttpCallback httpCallback;

            boolean byMultiThread = mTaskInfo.isByMultiThread();
            int rangeNum = mTaskInfo.getRangeNum();
            if (!byMultiThread) {
                String filePath = mTaskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                mTaskInfo.setCurrentSize(fileLength);
                httpCall = mClient.newCall(mResKey, mTaskInfo.getRequestUrl(), mTaskInfo.getCurrentSize());
                httpCallback = new SingleHttpCallbackImpl(mContext, mClient, mTaskInfo, mDownloadCallback, mFileChecker);
            } else {
                if (rangeNum == 1) {
                    String filePath = mTaskInfo.getFilePath();
                    long fileLength = DownloadFileHelper.getFileLength(filePath);
                    mTaskInfo.setCurrentSize(fileLength);
                    httpCall = mClient.newCall(mResKey, mTaskInfo.getRequestUrl(), mTaskInfo.getCurrentSize());
                    httpCallback = new SingleHttpCallbackImpl(mContext, mClient, mTaskInfo, mDownloadCallback, mFileChecker);
                } else {
                    httpCall = mClient.newCall(mResKey, mTaskInfo.getRequestUrl(), -1);
                    httpCallback = new MultiHttpCallbackImpl(mContext, mClient, mTaskInfo, mDownloadCallback, mFileChecker);
                }
            }
            mHttpCallback = httpCallback;
            httpCall.enqueue(httpCallback);
        }
    }

    void pause() {
        synchronized (mLock) {
            mIsPrepared = false;

            final AbstractHttpCallback callback = mHttpCallback;
            if (callback != null) {
                callback.cancel();
            }

            mTaskInfo.setCurrentStatus(State.PAUSE);
            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();

            mInnerTaskListener.onPause(mTaskInfo);

            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.PAUSE);
                message.obj = downloadInfo;
                sendMessage(message);
            } else {
                notifyPause(downloadInfo);
            }
        }
    }

    void delete() {
        synchronized (mLock) {
            mIsPrepared = false;

            AbstractHttpCallback callback = mHttpCallback;
            if (callback != null) {
                callback.cancel();
            }

            mTaskInfo.setCurrentStatus(State.DELETE);

            mInnerTaskListener.onDelete(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.DELETE);
                message.obj = downloadInfo;
                sendMessage(message);
            } else {
                notifyDelete(downloadInfo);
            }
        }
    }


    private void notifyPrepare(DownloadInfo downloadInfo) {
        mOuterTaskListener.onPrepare(downloadInfo);
    }

    private void notifyWaitingInQueue(DownloadInfo downloadInfo) {
        mOuterTaskListener.onWaitingInQueue(downloadInfo);
    }

    private void notifyConnected(DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields) {
        if (!checkPrepare()) return;

        mOuterTaskListener.onConnected(downloadInfo, responseHeaderFields);
    }

    private void notifyDownloading(long currentSize) {
        if (!checkPrepare()) return;

        String resKey = mResKey;
        long totalSize = mTaskInfo.getTotalSize();
        int progress = RangeUtil.computeProgress(currentSize, totalSize);
        float speed = mSpeed.computeSpeed(currentSize);
        mOuterTaskListener.onDownloading(resKey, totalSize, currentSize, progress, speed);
    }

    private void notifyRetrying(DownloadInfo downloadInfo, boolean deleteFile) {
        if (!checkPrepare()) return;

        mOuterTaskListener.onRetrying(downloadInfo, deleteFile);
    }


    private void notifyPause(DownloadInfo downloadInfo) {
        mOuterTaskListener.onPause(downloadInfo);
    }

    private void notifyDelete(DownloadInfo downloadInfo) {
        mOuterTaskListener.onDelete(downloadInfo);
    }

    private void notifySuccess(DownloadInfo downloadInfo) {
        if (!checkPrepare()) return;

        mOuterTaskListener.onSuccess(downloadInfo);
    }

    private void notifyFailure(DownloadInfo downloadInfo) {
        if (!checkPrepare()) return;

        mOuterTaskListener.onFailure(downloadInfo);
    }

    private boolean checkPrepare() {
        synchronized (mLock) {
            return mIsPrepared;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        TaskHandler that = (TaskHandler) object;
        return mResKey.equals(that.mResKey);
    }

    @SuppressWarnings("all")
    @Override
    public int compareTo(final TaskHandler taskHandler) {
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

    private void sendMessage(Message message) {
        if (mNotifyThread == null) {
            mPostHandler.sendMessage(message);
        } else {
            if (!mNotifyThread.isAlive()) {
                return;
            }
            try {
                mPostHandler.sendMessage(message);
            } catch (Exception e) {
                //
            }
        }
    }

    private class HandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            int state = msg.what;
            boolean needQuit = false;
            switch (state) {
                case State.PREPARE: {
                    notifyPrepare((DownloadInfo) msg.obj);
                    break;
                }
                case State.WAITING_IN_QUEUE: {
                    notifyWaitingInQueue((DownloadInfo) msg.obj);
                    break;
                }
                case State.CONNECTED: {
                    ConnectMessageObj connectMessageObj = (ConnectMessageObj) msg.obj;
                    notifyConnected(connectMessageObj.downloadInfo, connectMessageObj.responseHeaderFields);
                    break;
                }
                case State.DOWNLOADING: {
                    notifyDownloading((long) msg.obj);
                    break;
                }
                case State.RETRYING: {
                    notifyRetrying((DownloadInfo) msg.obj, msg.arg1 == 1);
                    break;
                }
                case State.PAUSE: {
                    notifyPause((DownloadInfo) msg.obj);
                    needQuit = true;
                    break;
                }
                case State.DELETE: {
                    notifyDelete((DownloadInfo) msg.obj);
                    needQuit = true;
                    break;
                }
                case State.SUCCESS: {
                    notifySuccess((DownloadInfo) msg.obj);
                    needQuit = true;
                    break;
                }
                case State.FAILURE: {
                    notifyFailure((DownloadInfo) msg.obj);
                    needQuit = true;
                    break;
                }
            }
            if (needQuit && mNotifyThread != null) {
                mNotifyThread.quit();
            }
            return true;
        }
    }

    private class DownloadCallbackImpl implements DownloadCallback {

        @Override
        public void onConnected(Map<String, List<String>> responseHeaderFields) {
            mInnerTaskListener.onConnected(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.CONNECTED);
                message.obj = new ConnectMessageObj(downloadInfo, responseHeaderFields);
                sendMessage(message);
            } else {
                notifyConnected(downloadInfo, responseHeaderFields);
            }
        }

        @Override
        public void onDownloading(long currentSize) {
            mInnerTaskListener.onDownloading(mTaskInfo);

            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.DOWNLOADING);
                message.obj = currentSize;
                sendMessage(message);
            } else {
                notifyDownloading(currentSize);
            }
        }

        @Override
        public void onRetrying(int failureCode, boolean deleteFile) {
            mInnerTaskListener.onRetrying(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.RETRYING);
                message.obj = downloadInfo;
                message.arg2 = deleteFile ? 1 : 0;
                sendMessage(message);
            } else {
                notifyRetrying(downloadInfo, deleteFile);
            }
        }

        @Override
        public void onSuccess() {
            mInnerTaskListener.onSuccess(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.SUCCESS);
                message.obj = downloadInfo;
                sendMessage(message);
            } else {
                notifySuccess(downloadInfo);
            }
        }

        @Override
        public void onFailure(int failureCode) {
            mInnerTaskListener.onFailure(mTaskInfo);

            DownloadInfo downloadInfo = mTaskInfo.toDownloadInfo();
            if (mPostHandler != null) {
                Message message = mPostHandler.obtainMessage(State.FAILURE);
                message.obj = downloadInfo;
                sendMessage(message);
            } else {
                notifyFailure(downloadInfo);
            }
        }
    }

    private static class ConnectMessageObj {

        DownloadInfo downloadInfo;

        Map<String, List<String>> responseHeaderFields;

        ConnectMessageObj(DownloadInfo downloadInfo, Map<String, List<String>> responseHeaderFields) {
            this.downloadInfo = downloadInfo;
            this.responseHeaderFields = responseHeaderFields;
        }
    }

    private static class Speed {

        private long lastFileSize;

        private long lastTimeMills;

        private float speed = 0.0f;

        float computeSpeed(long currentSize) {
            long elapsedTimeMillis = SystemClock.elapsedRealtime();
            if (lastTimeMills == 0) {
                lastFileSize = currentSize;
                lastTimeMills = elapsedTimeMillis;
                return 0.0f;
            } else {
                long diffTimeMillis = elapsedTimeMillis - lastTimeMills;
                if (speed == 0 || diffTimeMillis >= 2000) {
                    long diffSize = currentSize - lastFileSize;
                    lastFileSize = currentSize;
                    lastTimeMills = elapsedTimeMillis;
                    if (diffSize <= 0 || diffTimeMillis <= 0) {
                        return 0.0f;
                    }
                    speed = (diffSize * 1000.0f) / (diffTimeMillis * 1024.0f);
                }
                return speed;
            }
        }
    }
}