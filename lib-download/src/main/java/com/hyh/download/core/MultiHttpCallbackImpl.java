package com.hyh.download.core;

import android.content.Context;
import android.os.RemoteException;

import com.hyh.download.FailureCode;
import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.db.TaskDatabaseHelper;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.exception.ExceptionHelper;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.RangeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eric_He on 2019/1/1.
 */

public class MultiHttpCallbackImpl extends AbstractHttpCallback {

    private final Context context;

    private final HttpClient client;

    private final TaskInfo taskInfo;

    private final DownloadCallback downloadCallback;

    private final DownloadCallback downloadCallbackImpl = new DownloadCallbackImpl();

    private final IFileChecker fileChecker;

    private final IRetryStrategy retryStrategy;

    private volatile boolean cancel;

    private HttpCall call;

    private volatile AbstractHttpCallback downloadHttpCallback;

    private volatile boolean isConnected;

    private volatile boolean isRetryInvalidFileTask;

    MultiHttpCallbackImpl(Context context,
                          HttpClient client,
                          TaskInfo taskInfo,
                          DownloadCallback downloadCallback,
                          IFileChecker fileChecker) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.downloadCallback = downloadCallback;
        this.fileChecker = fileChecker;
        this.retryStrategy = new RetryStrategyImpl(context, taskInfo.isPermitRetryInMobileData());
    }

    @Override
    public void onResponse(HttpCall call, HttpResponse response) {
        this.call = call;
        if (cancel) {
            if (this.call != null && !this.call.isCanceled()) {
                this.call.cancel();
            }
            return;
        }

        if (!response.isSuccessful()) {
            if (!retryDownload(FailureCode.HTTP_ERROR, false)) {
                if (!cancel) {
                    //通知失败
                    notifyFailure(FailureCode.HTTP_ERROR);
                }
            }
            return;
        }

        int oldRangeNum = taskInfo.getRangeNum();
        if (oldRangeNum == 0) {//表示是一个新的下载
            long curTotalSize = response.contentLength();
            int curRangeNum = RangeUtil.computeRangeNum(curTotalSize);
            taskInfo.setRangeNum(curRangeNum);
            taskInfo.setTotalSize(curTotalSize);
            if (curRangeNum <= 1) {//单线程下载
                downloadHttpCallback = new SingleHttpCallbackImpl(context, client, taskInfo, downloadCallback, fileChecker);
                downloadHttpCallback.onResponse(call, response);
            } else {//多线程下载
                downloadHttpCallback = new MultiHttpCallbackWrapper(context, client, taskInfo, downloadCallbackImpl);

                handleConnected(response);

                List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, true);
                MultiHttpCallWrapper multiHttpCallWrapper = getMultiHttpCallWrapper(client, taskInfo, rangeInfoList);

                if (multiHttpCallWrapper.isHttpCallEmpty()) {
                    handleSuccess();
                } else {
                    multiHttpCallWrapper.enqueue(downloadHttpCallback);
                }
            }
        } else {//表示之前是多线程下载
            downloadHttpCallback = new MultiHttpCallbackWrapper(context, client, taskInfo, downloadCallbackImpl);

            handleConnected(response);

            List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, false);
            MultiHttpCallWrapper multiHttpCallWrapper = getMultiHttpCallWrapper(client, taskInfo, rangeInfoList);

            if (multiHttpCallWrapper.isHttpCallEmpty()) {
                handleSuccess();
            } else {
                multiHttpCallWrapper.enqueue(downloadHttpCallback);
            }
        }
    }

    @Override
    public void onFailure(HttpCall call, Exception e) {
        int failureCode = ExceptionHelper.convertFailureCode(e);
        if (!retryDownload(failureCode, false)) {
            if (!cancel) {
                //通知失败
                notifyFailure(failureCode);
            }
        }
    }

    @Override
    void cancel() {
        cancel = true;
        retryStrategy.cancel();
        if (downloadHttpCallback != null) {
            downloadHttpCallback.cancel();
        }
    }

    private void handleConnected(HttpResponse response) {
        isConnected = true;

        DownloadFileHelper.fixTaskFilePath(response, taskInfo);

        taskInfo.setTargetUrl(response.url());
        taskInfo.setContentMD5(response.header(NetworkHelper.CONTENT_MD5));
        taskInfo.setContentType(response.header(NetworkHelper.CONTENT_TYPE));
        taskInfo.setETag(response.header(NetworkHelper.ETAG));
        taskInfo.setLastModified(response.header(NetworkHelper.LAST_MODIFIED));

        notifyConnected(response.headers());
    }

    private void handleSuccess() {
        if (checkSuccessFile()) {
            notifySuccess();
        } else {
            if (!isRetryInvalidFileTask && taskInfo.isPermitRetryInvalidFileTask()) {
                isRetryInvalidFileTask = true;
                DownloadFileHelper.deleteDownloadFile(taskInfo);
                TaskDatabaseHelper.getInstance().insertOrUpdate(taskInfo);
                if (!retryDownload(FailureCode.FILE_CHECK_FAILURE, true)) {
                    notifyFailure(FailureCode.FILE_CHECK_FAILURE);
                }
            } else {
                notifyFailure(FailureCode.FILE_CHECK_FAILURE);
            }
        }
    }

    private void notifyConnected(Map<String, List<String>> responseHeaderFields) {
        taskInfo.setCurrentStatus(State.CONNECTED);
        downloadCallback.onConnected(responseHeaderFields);
    }

    private void notifyDownloading(long currentSize) {
        taskInfo.setCurrentStatus(State.DOWNLOADING);
        downloadCallback.onDownloading(currentSize);
    }

    private void notifyRetrying(int failureCode, boolean deleteFile) {
        taskInfo.setCurrentStatus(State.RETRYING);
        taskInfo.setFailureCode(failureCode);
        downloadCallback.onRetrying(failureCode, deleteFile);
    }

    private void notifySuccess() {
        taskInfo.setCurrentStatus(State.SUCCESS);
        downloadCallback.onSuccess();
    }

    private void notifyFailure(int failureCode) {
        taskInfo.setCurrentStatus(State.FAILURE);
        taskInfo.setFailureCode(failureCode);
        downloadCallback.onFailure(failureCode);
    }


    private boolean retryDownload(final int failureCode, final boolean deleteFile) {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        if (isConnected) {
            notifyRetrying(failureCode, deleteFile);
        }
        boolean shouldRetry = retryStrategy.shouldRetry();
        if (shouldRetry) {
            if (!cancel) {
                HttpCall call = client.newCall(taskInfo.getResKey(), taskInfo.getRequestUrl(), -1);
                call.enqueue(this);
            }
        }
        return shouldRetry;
    }


    private class DownloadCallbackImpl implements DownloadCallback {

        @Override
        public void onConnected(Map<String, List<String>> responseHeaderFields) {
        }

        @Override
        public void onDownloading(long currentSize) {
            notifyDownloading(currentSize);
        }

        @Override
        public void onRetrying(int failureCode, boolean deleteFile) {
            notifyRetrying(failureCode, deleteFile);
        }

        @Override
        public void onSuccess() {
            handleSuccess();
        }

        @Override
        public void onFailure(int failureCode) {
            notifyFailure(failureCode);
        }
    }

    private boolean checkSuccessFile() {
        if (fileChecker == null) {
            return true;
        }
        try {
            return fileChecker.isValidFile(taskInfo.toDownloadInfo());
        } catch (RemoteException e) {
            return true;
        }
    }

    private List<RangeInfo> getRangeInfoList(TaskInfo taskInfo, boolean isNewTask) {
        long totalSize = taskInfo.getTotalSize();
        int rangeNum = taskInfo.getRangeNum();

        String filePath = DownloadFileHelper.getTaskFilePath(taskInfo);

        List<RangeInfo> rangeInfoList = new ArrayList<>(rangeNum);
        if (isNewTask) {
            long[] startPositions = RangeUtil.computeStartPositions(totalSize, rangeNum);
            long[] endPositions = RangeUtil.computeEndPositions(totalSize, rangeNum);
            for (int index = 0; index < rangeNum; index++) {
                String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
                rangeInfoList.add(new RangeInfo(index, tempFilePath, startPositions[index], startPositions[index], endPositions[index]));
            }
        } else {
            long[] endPositions = RangeUtil.computeEndPositions(totalSize, rangeNum);

            long[] originalStartPositions = RangeUtil.computeStartPositions(totalSize, rangeNum);
            long[] startPositions = RangeUtil.getCacheStartPositions(filePath, originalStartPositions, endPositions);
            if (startPositions == null) {
                startPositions = originalStartPositions;
            }
            long currentSize = 0;
            for (int index = 0; index < rangeNum; index++) {
                String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
                long startPosition = startPositions[index];
                long endPosition = endPositions[index];
                rangeInfoList.add(new RangeInfo(index, tempFilePath, originalStartPositions[index], startPosition, endPosition));
                long rangeSize = startPosition - originalStartPositions[index];
                currentSize += rangeSize;
            }
            taskInfo.setCurrentSize(currentSize);
        }
        return rangeInfoList;
    }

    private MultiHttpCallWrapper getMultiHttpCallWrapper(HttpClient client, TaskInfo taskInfo, List<RangeInfo> rangeInfoList) {
        String resKey = taskInfo.getResKey();
        Map<String, HttpCall> httpCallMap = new HashMap<>();
        for (RangeInfo rangeInfo : rangeInfoList) {
            long startPosition = rangeInfo.getStartPosition();
            long endPosition = rangeInfo.getEndPosition();
            if (startPosition <= endPosition) {
                String tag = resKey.concat("-").concat(String.valueOf(rangeInfo.getRangeIndex()));
                httpCallMap.put(tag, client.newCall(tag, taskInfo.getRequestUrl(), startPosition, endPosition));
            }
        }
        return new MultiHttpCallWrapper(httpCallMap, rangeInfoList);
    }
}
