package com.hyh.download.core;


import com.hyh.download.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */

class HttpCallFactory {

    private Map<String, Integer> mGetTotalSizeRetryTimesMap = new HashMap<>();

    void create(HttpClient client, TaskInfo taskInfo, HttpCallCreateListener listener) {
        String resKey = taskInfo.getResKey();
        mGetTotalSizeRetryTimesMap.put(resKey, 0);
        String url = taskInfo.getRequestUrl();
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum <= 1) {
            listener.onCreateFinish(client.newCall(resKey, url, taskInfo.getCurrentSize()));
        } else {
            HttpCall httpCall = null;
            long totalSize = getTotalSize(client, taskInfo);
            if (totalSize > 0) {
                long[] startPositions = taskInfo.getStartPositions();
                if (startPositions == null) {
                    startPositions = computeStartPositions(totalSize, rangeNum);
                    taskInfo.setStartPositions(startPositions);
                }
                long[] endPositions = taskInfo.getEndPositions();
                if (endPositions == null) {
                    endPositions = computeEndPositions(totalSize, rangeNum);
                    taskInfo.setEndPositions(endPositions);
                }

                Map<String, HttpCall> httpCallMap = new HashMap<>();
                for (int index = 0; index < rangeNum; index++) {
                    long startPosition = startPositions[index];
                    long endPosition = endPositions[index];
                    if (startPosition < endPosition) {
                        String tag = resKey.concat("-").concat(String.valueOf(index));
                        httpCallMap.put(tag, client.newCall(tag, url, startPosition, endPosition));
                    }
                }
                httpCall = new MultiHttpCall(httpCallMap);
            }
            int code = taskInfo.getResponseCode();
            if (code == Constants.ResponseCode.OK) {//无法获取到文件长度，不能进行多线程下载
                taskInfo.setRangeNum(1);//设置为单线程下载
                httpCall = client.newCall(resKey, url, taskInfo.getCurrentSize());
            }
            listener.onCreateFinish(httpCall);
        }
    }

    private long getTotalSize(HttpClient client, TaskInfo taskInfo) {
        long totalSize = taskInfo.getTotalSize();
        if (totalSize == 0) {
            try {
                HttpResponse httpResponse = client.getHttpResponse(taskInfo.getRequestUrl());
                if (httpResponse.code() == Constants.ResponseCode.OK) {
                    totalSize = httpResponse.contentLength();
                    if (totalSize > 0) {
                        taskInfo.setTotalSize(totalSize);
                    }
                }
                taskInfo.setResponseCode(httpResponse.code());
            } catch (Exception e) {
                e.printStackTrace();
                String resKey = taskInfo.getResKey();
                Integer retryTimes = mGetTotalSizeRetryTimesMap.get(resKey);
                mGetTotalSizeRetryTimesMap.put(resKey, retryTimes + 1);
                if (retryTimes < 3) {
                    return getTotalSize(client, taskInfo);
                }
            }
        }
        return totalSize;
    }

    private long[] computeStartPositions(long totalSize, int rangeNum) {
        long[] startPositions = new long[rangeNum];
        long rangeSize = totalSize / rangeNum;
        for (int index = 0; index < rangeNum; index++) {
            if (index == 0) {
                startPositions[index] = 0;
            } else {
                startPositions[index] = rangeSize * index + 1;
            }
        }
        return startPositions;
    }

    private long[] computeEndPositions(long totalSize, int rangeNum) {
        long[] endPositions = new long[rangeNum];
        long rangeSize = totalSize / rangeNum;
        for (int index = 0; index < rangeNum; index++) {
            if (index < rangeNum - 1) {
                endPositions[index] = rangeSize * (index + 1);
            } else {
                endPositions[index] = totalSize - 1;
            }
        }
        return endPositions;
    }


    public interface HttpCallCreateListener {

        void onCreateFinish(HttpCall call);

    }
}
