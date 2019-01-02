package com.hyh.download.core;

import android.content.Context;

import com.hyh.download.db.bean.TaskInfo;
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


    private Context context;

    private HttpClient client;

    private TaskInfo taskInfo;

    private DownloadCallback downloadCallback;

    private IRetryStrategy retryStrategy;

    private volatile boolean cancel;

    private HttpCall call;

    private volatile AbstractHttpCallback downloadHttpCallback;

    public MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, DownloadCallback downloadCallback) {
        this.context = context;
        this.client = client;
        this.taskInfo = taskInfo;
        this.downloadCallback = downloadCallback;
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
            if (!retryDownload()) {
                if (!cancel) {
                    //通知失败
                    handleFailure();
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
                downloadHttpCallback = new SingleHttpCallbackImpl(context, client, taskInfo, downloadCallback);
                downloadHttpCallback.onResponse(call, response);
            } else {//多线程下载
                downloadHttpCallback = new MultiHttpCallbackWrapper(context, client, taskInfo, downloadCallback);

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
            downloadHttpCallback = new MultiHttpCallbackWrapper(context, client, taskInfo, downloadCallback);

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
        if (!retryDownload()) {
            if (!cancel) {
                //通知失败
                handleFailure();
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
        DownloadFileHelper.fixFilePath(response, taskInfo);
        taskInfo.setTargetUrl(response.url());
        taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
        taskInfo.setCacheTargetUrl(response.url());
        taskInfo.setETag(response.header(NetworkHelper.ETAG));
        taskInfo.setLastModified(response.header(NetworkHelper.LAST_MODIFIED));
        downloadCallback.onConnected(taskInfo, response.headers());
    }

    private void handleSuccess() {
        downloadCallback.onSuccess(taskInfo);
    }

    private void handleFailure() {
        downloadCallback.onFailure(taskInfo);
    }


    private boolean retryDownload() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        boolean shouldRetry = retryStrategy.shouldRetry(new IRetryStrategy.onWaitingListener() {
            @Override
            public void onWaiting() {
            }
        });
        if (shouldRetry) {
            if (!cancel) {
                HttpCall call = client.newCall(taskInfo.getResKey(), taskInfo.getRequestUrl(), -1);
                call.enqueue(this);
            }
        }
        return shouldRetry;
    }

    private List<RangeInfo> getRangeInfoList(TaskInfo taskInfo, boolean isNewTask) {
        long totalSize = taskInfo.getTotalSize();
        int rangeNum = taskInfo.getRangeNum();
        String filePath = taskInfo.getFilePath();
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
            taskInfo.setProgress(RangeUtil.computeProgress(currentSize, taskInfo.getTotalSize()));
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
