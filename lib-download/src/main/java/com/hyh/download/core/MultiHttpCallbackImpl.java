package com.hyh.download.core;

import android.content.Context;

import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.RangeUtil;

/**
 * Created by Eric_He on 2019/1/1.
 */

public class MultiHttpCallbackImpl extends AbstractHttpCallback {

    private HttpClient client;

    private TaskInfo taskInfo;

    private DownloadCallback downloadCallback;

    private IRetryStrategy retryStrategy;

    private volatile boolean cancel;

    private HttpCall call;

    private volatile AbstractHttpCallback downloadHttpCallback;

    public MultiHttpCallbackImpl(Context context, HttpClient client, TaskInfo taskInfo, DownloadCallback downloadCallback) {
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
                //通知失败
            }
            return;
        }

        DownloadFileHelper.fixFilePath(response, taskInfo);
        taskInfo.setTargetUrl(response.url());
        taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
        taskInfo.setCacheTargetUrl(response.url());
        taskInfo.setETag(response.header(NetworkHelper.ETAG));

        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum == 0) {//表示是一个新的下载
            long curTotalSize = response.contentLength();
            rangeNum = RangeUtil.computeRangeNum(curTotalSize);
            taskInfo.setRangeNum(rangeNum);
            taskInfo.setTotalSize(curTotalSize);
            if (rangeNum <= 1) {//单线程下载

            } else {//多线程下载

            }
        } else {//表示之前是多线程下载

        }
    }

    @Override
    public void onFailure(HttpCall call, Exception e) {

    }

    @Override
    void cancel() {
        cancel = true;
        if (downloadHttpCallback != null) {
            downloadHttpCallback.cancel();
        }
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
            HttpCall call = client.newCall(taskInfo.getResKey(), taskInfo.getRequestUrl(), -1);
            call.enqueue(this);
        }
        return shouldRetry;
    }
}
