package com.hyh.download.core;


import android.text.TextUtils;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */

class HttpCallFactory {

    private final ThreadPoolExecutor mExecutor;

    private Map<String, Integer> mGetTotalSizeRetryTimesMap = new HashMap<>();

    private List<String> mCreatingList = new CopyOnWriteArrayList<>();

    HttpCallFactory(ThreadPoolExecutor executor) {
        mExecutor = executor;
    }

    void create(final HttpClient client, final TaskInfo taskInfo, final HttpCallCreateListener listener) {
        final String resKey = taskInfo.getResKey();
        mCreatingList.add(resKey);
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HttpCall httpCall = getHttpCall(client, taskInfo);
                listener.onCreateFinish(httpCall);
                mCreatingList.remove(resKey);
            }
        });
    }

    boolean isCreating(String resKey) {
        return mCreatingList.contains(resKey);
    }

    private HttpCall getHttpCall(HttpClient client, TaskInfo taskInfo) {
        String resKey = taskInfo.getResKey();
        String url = taskInfo.getRequestUrl();
        mGetTotalSizeRetryTimesMap.put(resKey, 0);
        if (taskInfo.isByMultiThread()) {
            int rangeNum = taskInfo.getRangeNum();
            if (rangeNum == 0) {//表示是一个新的下载
                HttpResponse response = requestFileInfo(client, taskInfo);
                if (response == null) {
                    return null;
                }
                int code = response.code();
                if (code != Constants.ResponseCode.OK) {
                    return null;
                }
                DownloadFileHelper.fixFilePath(response, taskInfo);
                taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
                taskInfo.setCacheTargetUrl(response.url());

                long curTotalSize = response.contentLength();
                rangeNum = RangeUtil.computeRangeNum(curTotalSize);
                taskInfo.setRangeNum(rangeNum);
                taskInfo.setTotalSize(curTotalSize);
                taskInfo.setETag(response.header(NetworkHelper.ETAG));
                if (rangeNum <= 1) {
                    return client.newCall(resKey, url, taskInfo.getCurrentSize());
                } else {
                    List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, true);
                    return getMultiHttpCall(client, taskInfo, rangeInfoList);
                }
            } else if (rangeNum == 1) {//表示之前是使用一个线程下载
                String filePath = taskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                taskInfo.setCurrentSize(fileLength);
                taskInfo.setProgress(RangeUtil.computeProgress(fileLength, taskInfo.getTotalSize()));
                return client.newCall(resKey, url, taskInfo.getCurrentSize());
            } else {
                long cacheTotalSize = taskInfo.getTotalSize();
                HttpResponse response = requestFileInfo(client, taskInfo);
                if (response == null) {
                    return null;
                }
                int code = response.code();
                if (code != Constants.ResponseCode.OK) {
                    return null;
                }
                DownloadFileHelper.fixFilePath(response, taskInfo);
                taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
                taskInfo.setCacheTargetUrl(response.url());

                long curTotalSize = response.contentLength();
                String curETag = response.header(NetworkHelper.ETAG);

                if (cacheTotalSize == curTotalSize && (taskInfo.getETag() == null || TextUtils.equals(taskInfo.getETag(), curETag))) {
                    List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, false);
                    return getMultiHttpCall(client, taskInfo, rangeInfoList);
                } else {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    taskInfo.setTotalSize(curTotalSize);
                    taskInfo.setETag(response.header(NetworkHelper.ETAG));
                    if (curTotalSize > 0) {
                        //重新计算下载区间数
                        rangeNum = RangeUtil.computeRangeNum(curTotalSize);
                        taskInfo.setRangeNum(rangeNum);
                        List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, true);
                        return getMultiHttpCall(client, taskInfo, rangeInfoList);
                    } else {
                        //没有获取到下载大小，采用单线程下载
                        rangeNum = 1;
                        taskInfo.setRangeNum(rangeNum);
                        return client.newCall(resKey, url, taskInfo.getCurrentSize());
                    }
                }
            }
        } else {
            long fileLength = DownloadFileHelper.getFileLength(taskInfo.getFilePath());
            taskInfo.setCurrentSize(fileLength);
            taskInfo.setProgress(RangeUtil.computeProgress(fileLength, taskInfo.getTotalSize()));
            return client.newCall(resKey, url, fileLength);
        }
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
            long[] originalStartPositions = RangeUtil.computeStartPositions(totalSize, rangeNum);
            long[] startPositions = RangeUtil.getCacheStartPositions(filePath, originalStartPositions);
            if (startPositions == null) {
                startPositions = originalStartPositions;
            }
            long currentSize = 0;
            long[] endPositions = RangeUtil.computeEndPositions(totalSize, rangeNum);
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

    private MultiHttpCall getMultiHttpCall(HttpClient client, TaskInfo taskInfo, List<RangeInfo> rangeInfoList) {
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
        return new MultiHttpCall(httpCallMap, rangeInfoList);
    }

    private HttpResponse requestFileInfo(HttpClient client, TaskInfo taskInfo) {
        try {
            return client.getHttpResponse(taskInfo.getRequestUrl());
        } catch (Exception e) {
            //
        }
        Integer retryTimes = mGetTotalSizeRetryTimesMap.get(taskInfo.getResKey());
        if (retryTimes != null && retryTimes >= 3) {
            return null;
        }
        if (retryTimes == null) {
            retryTimes = 1;
        } else {
            retryTimes++;
        }
        mGetTotalSizeRetryTimesMap.put(taskInfo.getResKey(), retryTimes);
        return requestFileInfo(client, taskInfo);
    }

    public interface HttpCallCreateListener {

        void onCreateFinish(HttpCall call);

    }
}
