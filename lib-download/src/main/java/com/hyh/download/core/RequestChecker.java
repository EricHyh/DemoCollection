package com.hyh.download.core;

import android.content.Context;
import android.text.TextUtils;

import com.hyh.download.IFileChecker;
import com.hyh.download.State;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;

/**
 * Created by Eric_He on 2019/1/15.
 */

class RequestChecker {

    private final Context mContext;

    private final DownloadProxyImpl mDownloadProxyImpl;

    private final DownloadProxyConfig mDownloadProxyConfig;

    RequestChecker(Context context,
                   DownloadProxyImpl downloadProxyImpl,
                   DownloadProxyConfig downloadProxyConfig) {
        this.mContext = context;
        this.mDownloadProxyImpl = downloadProxyImpl;
        this.mDownloadProxyConfig = downloadProxyConfig;
    }

    TaskInfo check(RequestInfo requestInfo, IFileChecker fileChecker) {
        return null;
    }

    private TaskInfo getTaskInfo(RequestInfo requestInfo, IFileChecker fileChecker) {
        String resKey = requestInfo.resKey;
        TaskInfo taskInfo = mDownloadProxyImpl.getTaskInfoByKey(resKey);
        if (taskInfo != null) {
            if (!isRequestChanged(requestInfo, taskInfo)) {
                fixRequestInfo(requestInfo, taskInfo);
            } else {
                if (mDownloadProxyImpl.isFileDownloaded(resKey, fileChecker)) {
                    taskInfo = newTaskInfo(requestInfo);
                } else {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    taskInfo = newTaskInfo(requestInfo);
                }
            }
        } else {
            taskInfo = newTaskInfo(requestInfo);
        }
        taskInfo.setCurrentStatus(State.NONE);
        return taskInfo;
    }

    private boolean isRequestChanged(RequestInfo requestInfo, TaskInfo taskInfo) {
        return requestInfo.forceDownload
                || isUrlChanged(requestInfo, taskInfo)
                || isFilePathChanged(requestInfo, taskInfo);
    }

    private boolean isUrlChanged(RequestInfo requestInfo, TaskInfo taskInfo) {
        return requestInfo.needVerifyUrl && !TextUtils.equals(requestInfo.url, taskInfo.getRequestUrl());
    }

    private boolean isFilePathChanged(RequestInfo requestInfo, TaskInfo taskInfo) {
        String requestFileDir = requestInfo.fileDir;
        String requestFileName = requestInfo.fileName;
        String cacheFileDir = taskInfo.getFileDir();
        String cacheFileName = taskInfo.getFileName();
        if (!TextUtils.isEmpty(requestFileDir) && !TextUtils.equals(cacheFileDir, requestFileDir)) {
            L.d("requestFileDir:" + requestFileDir + ", cacheFileDir:" + cacheFileDir);
            return true;
        }
        if (!TextUtils.isEmpty(requestFileName) && !TextUtils.equals(cacheFileDir, cacheFileName)) {
            L.d("requestFileName:" + requestFileName + ", cacheFileName:" + cacheFileName);
            return true;
        }
        return false;
    }

    private void fixRequestInfo(RequestInfo requestInfo, TaskInfo taskInfo) {
        taskInfo.setRequestUrl(requestInfo.url);
        taskInfo.setTargetUrl(null);

        taskInfo.setWifiAutoRetry(requestInfo.wifiAutoRetry);
        taskInfo.setPermitRetryInMobileData(requestInfo.permitRetryInMobileData);
        taskInfo.setPermitRetryInvalidFileTask(requestInfo.permitRetryInvalidFileTask);
        taskInfo.setPermitRecoverTask(requestInfo.permitRecoverTask);

        taskInfo.setResponseCode(0);
        taskInfo.setFailureCode(0);
        taskInfo.setTag(requestInfo.tag);
    }

    private TaskInfo newTaskInfo(RequestInfo requestInfo) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setResKey(requestInfo.resKey);
        taskInfo.setRequestUrl(requestInfo.url);

        String fileDir = createFileDir(requestInfo);
        taskInfo.setFileDir(fileDir);
        String fileName = requestInfo.fileName;

        fileName = DownloadFileHelper.fixFileExists(fileDir, fileName);

        taskInfo.setFileName(fileName);
        taskInfo.setByMultiThread(requestInfo.byMultiThread);

        taskInfo.setWifiAutoRetry(requestInfo.wifiAutoRetry);
        taskInfo.setPermitRetryInMobileData(requestInfo.permitRetryInMobileData);
        taskInfo.setPermitRetryInvalidFileTask(requestInfo.permitRetryInvalidFileTask);
        taskInfo.setPermitRecoverTask(requestInfo.permitRecoverTask);

        taskInfo.setTag(requestInfo.tag);
        return taskInfo;
    }

    private String createFileDir(RequestInfo requestInfo) {
        String fileDir = requestInfo.fileDir;
        if (TextUtils.isEmpty(fileDir)) {
            fileDir = mDownloadProxyConfig.getDefaultFileDir();
        }
        if (TextUtils.isEmpty(fileDir)) {
            fileDir = DownloadFileHelper.getDefaultFileDir(mContext);
        }
        DownloadFileHelper.ensureCreated(fileDir);
        return fileDir;
    }
}
