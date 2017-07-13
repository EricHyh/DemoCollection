package com.eric.hyh.tools.download.internal;

import com.eric.hyh.tools.download.api.HttpCall;
import com.eric.hyh.tools.download.api.HttpClient;
import com.eric.hyh.tools.download.api.HttpResponse;
import com.eric.hyh.tools.download.bean.TaskInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2017/7/13
 */

public class HttpCallFactory {


    private static int sGetTotalSizeRetryTimes = 0;

    public static HttpCall produce(HttpClient client, TaskInfo taskInfo) {
        sGetTotalSizeRetryTimes = 0;
        String resKey = taskInfo.getResKey();
        String url = taskInfo.getUrl();
        int rangeNum = taskInfo.getRangeNum();
        if (rangeNum <= 1) {
            return client.newCall(resKey, url, taskInfo.getCurrentSize());
        } else {
            long totalSize = getTotalSize(client, taskInfo);
            if (totalSize > 0) {
                long[] startPositions = taskInfo.getStartPositions();
                if (startPositions == null) {
                    startPositions = computeStartPositions(totalSize, rangeNum);
                }
                taskInfo.setStartPositions(startPositions);
                long[] endPositions = computeEndPositions(totalSize, rangeNum);
                Map<String, HttpCall> httpCallMap = new HashMap<>();
                for (int index = 0; index < rangeNum; index++) {
                    long startPosition = startPositions[index];
                    long endPosition = endPositions[index];
                    if (startPosition < endPosition) {
                        String tag = resKey.concat("-").concat(String.valueOf(index));
                        httpCallMap.put(tag, client.newCall(tag, url, startPosition, endPosition));
                    }
                }
                return new MultiHttpCall(httpCallMap);
            }
        }
        return null;
    }

    private static long getTotalSize(HttpClient client, TaskInfo taskInfo) {
        long totalSize = taskInfo.getTotalSize();
        if (totalSize == 0) {
            try {
                HttpResponse httpResponse = client.getHttpResponse(taskInfo.getUrl());
                if (httpResponse.code() == Constans.ResponseCode.OK) {
                    totalSize = httpResponse.contentLength();
                    if (totalSize > 0) {
                        taskInfo.setTotalSize(totalSize);
                    }
                }
                taskInfo.setCode(httpResponse.code());
            } catch (IOException e) {
                e.printStackTrace();
                if (sGetTotalSizeRetryTimes++ < 3) {
                    return getTotalSize(client, taskInfo);
                }
            }
        }
        return totalSize;
    }

    private static long[] computeStartPositions(long totalSize, int rangeNum) {
        long[] startPositions = new long[rangeNum];
        long rangeSize = rangeNum / totalSize;
        for (int index = 0; index < rangeNum; index++) {
            if (index == 0) {
                startPositions[index] = 0;
            } else {
                startPositions[index] = rangeSize * index + 1;
            }
        }
        return startPositions;
    }

    private static long[] computeEndPositions(long totalSize, int rangeNum) {
        long[] endPositions = new long[rangeNum];
        long rangeSize = rangeNum / totalSize;
        for (int index = 0; index < rangeNum; index++) {
            if (index < rangeNum - 1) {
                endPositions[index] = rangeSize * (index + 1);
            } else {
                endPositions[index] = totalSize;
            }
        }
        return endPositions;
    }

}
