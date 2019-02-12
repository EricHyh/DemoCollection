package com.hyh.download.core;

import android.content.Context;
import android.text.TextUtils;

import com.hyh.download.FailureCode;
import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.L;

/**
 * Created by Eric_He on 2019/1/15.
 */

class RequestChecker {

    private final Context mContext;

    private final DownloadProxyImpl mDownloadProxyImpl;

    RequestChecker(Context context, DownloadProxyImpl downloadProxyImpl) {
        this.mContext = context;
        this.mDownloadProxyImpl = downloadProxyImpl;
    }

    TaskInfo check(RequestInfo requestInfo) {
        String resKey = requestInfo.resKey;
        TaskInfo taskInfo = mDownloadProxyImpl.getTaskInfoByKey(resKey);
        if (taskInfo != null) {
            if (!isRequestChanged(requestInfo, taskInfo)) {
                fixRequestInfo(requestInfo, taskInfo);
            } else {
                if (mDownloadProxyImpl.isFileDownloadedWithoutCheck(resKey)) {
                    taskInfo = newTaskInfo(requestInfo);
                } else {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    taskInfo = newTaskInfo(requestInfo);
                }
            }
        } else {
            taskInfo = newTaskInfo(requestInfo);
        }
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
        String cacheRequestFileName = taskInfo.getRequestFileName();
        String cacheRealFileName = taskInfo.getRealFileName();

        if (!TextUtils.isEmpty(requestFileDir) && !TextUtils.equals(cacheFileDir, requestFileDir)) {
            L.d("requestFileDir:" + requestFileDir + ", cacheFileDir:" + cacheFileDir);
            return true;
        }
        if (!TextUtils.isEmpty(requestFileName)
                && !TextUtils.equals(requestFileName, cacheRequestFileName)
                && !TextUtils.equals(requestFileName, cacheRealFileName)) {
            L.d("requestFileName:" + requestFileName
                    + ", cacheRequestFileName:" + cacheRequestFileName
                    + ", cacheRealFileName:" + cacheRealFileName);
            return true;
        }
        return false;
    }

    private void fixRequestInfo(RequestInfo requestInfo, TaskInfo taskInfo) {
        taskInfo.setRequestUrl(requestInfo.url);

        taskInfo.setWifiAutoRetry(requestInfo.wifiAutoRetry);
        taskInfo.setPermitRetryInMobileData(requestInfo.permitRetryInMobileData);
        taskInfo.setPermitRetryInvalidFileTask(requestInfo.permitRetryInvalidFileTask);
        taskInfo.setPermitRecoverTask(requestInfo.permitRecoverTask);

        taskInfo.setResponseCode(0);
        taskInfo.setFailureCode(FailureCode.NO_FAILURE);
        taskInfo.setTag(requestInfo.tag);
    }

    private TaskInfo newTaskInfo(RequestInfo requestInfo) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setResKey(requestInfo.resKey);
        taskInfo.setRequestUrl(requestInfo.url);

        String fileDir = requestInfo.fileDir;
        taskInfo.setFileDir(fileDir);
        String fileName = requestInfo.fileName;
        if (!TextUtils.isEmpty(fileName)) {
            taskInfo.setRequestFileName(fileName);
            boolean isFileExists = DownloadFileHelper.isFileExists(fileDir, fileName);
            if (isFileExists) {
                if (requestInfo.autoRenameFile) {
                    fileName = DownloadFileHelper.fixFileExists(fileDir, fileName);
                } else {
                    taskInfo.setFailureCode(FailureCode.FILE_NAME_CONFLICT);
                }
            }
            taskInfo.setRealFileName(fileName);
        }

        taskInfo.setByMultiThread(requestInfo.byMultiThread);
        taskInfo.setWifiAutoRetry(requestInfo.wifiAutoRetry);
        taskInfo.setPermitRetryInMobileData(requestInfo.permitRetryInMobileData);
        taskInfo.setPermitRetryInvalidFileTask(requestInfo.permitRetryInvalidFileTask);
        taskInfo.setPermitRecoverTask(requestInfo.permitRecoverTask);

        taskInfo.setTag(requestInfo.tag);
        return taskInfo;
    }
}
