package com.hyh.download.core;


import android.text.TextUtils;
import android.webkit.URLUtil;

import com.hyh.download.db.bean.TaskInfo;
import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.DownloadFileHelper;
import com.hyh.download.utils.NetworkHelper;
import com.hyh.download.utils.ProgressHelper;
import com.hyh.download.utils.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        String url = taskInfo.getRequestUrl();
        mGetTotalSizeRetryTimesMap.put(resKey, 0);
        if (taskInfo.isByMultiThread()) {
            int rangeNum = taskInfo.getRangeNum();
            if (rangeNum == 0) {//表示是一个新的下载
                Long curTotalSize = requestFileInfo(client, taskInfo);
                if (curTotalSize == null) {
                    listener.onCreateFinish(null);
                    return;
                }
                rangeNum = computeRangeNum(curTotalSize);
                taskInfo.setRangeNum(rangeNum);
                taskInfo.setTotalSize(curTotalSize);

                List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, true);
                MultiHttpCall multiHttpCall = getMultiHttpCall(client, taskInfo, rangeInfoList);

                listener.onCreateFinish(multiHttpCall);
            } else if (rangeNum == 1) {//表示之前是使用一个线程下载
                String filePath = taskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                taskInfo.setCurrentSize(fileLength);
                taskInfo.setProgress(ProgressHelper.computeProgress(fileLength, taskInfo.getTotalSize()));
                listener.onCreateFinish(client.newCall(resKey, url, taskInfo.getCurrentSize()));
            } else {
                long cacheTotalSize = taskInfo.getTotalSize();
                Long curTotalSize = requestFileInfo(client, taskInfo);
                if (curTotalSize == null) {
                    listener.onCreateFinish(null);
                    return;
                }
                String filePath = taskInfo.getFilePath();
                long fileLength = DownloadFileHelper.getFileLength(filePath);
                if (cacheTotalSize == curTotalSize && fileLength == curTotalSize) {

                    List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, false);
                    MultiHttpCall multiHttpCall = getMultiHttpCall(client, taskInfo, rangeInfoList);

                    listener.onCreateFinish(multiHttpCall);
                } else {
                    DownloadFileHelper.deleteDownloadFile(taskInfo);
                    taskInfo.setTotalSize(curTotalSize);
                    taskInfo.setCurrentSize(0);
                    taskInfo.setProgress(0);
                    taskInfo.setETag(null);
                    if (curTotalSize > 0) {
                        //重新计算下载区间数
                        rangeNum = computeRangeNum(curTotalSize);
                        taskInfo.setRangeNum(rangeNum);

                        List<RangeInfo> rangeInfoList = getRangeInfoList(taskInfo, true);
                        MultiHttpCall multiHttpCall = getMultiHttpCall(client, taskInfo, rangeInfoList);

                        listener.onCreateFinish(multiHttpCall);
                    } else {
                        //没有获取到下载大小，采用单线程下载
                        rangeNum = 1;
                        taskInfo.setRangeNum(rangeNum);
                        listener.onCreateFinish(client.newCall(resKey, url, taskInfo.getCurrentSize()));
                    }
                }
            }
        } else {
            listener.onCreateFinish(client.newCall(resKey, url, taskInfo.getCurrentSize()));
        }
    }

    private List<RangeInfo> getRangeInfoList(TaskInfo taskInfo, boolean isNewTask) {
        long totalSize = taskInfo.getTotalSize();
        int rangeNum = taskInfo.getRangeNum();
        String filePath = taskInfo.getFilePath();
        List<RangeInfo> rangeInfoList = new ArrayList<>(rangeNum);
        if (isNewTask) {
            long[] startPositions = computeStartPositions(totalSize, rangeNum);
            long[] endPositions = computeEndPositions(totalSize, rangeNum);
            for (int index = 0; index < rangeNum; index++) {
                String rangeFilePath = DownloadFileHelper.getRangeFilePath(filePath, index);
                rangeInfoList.add(new RangeInfo(index, rangeFilePath, startPositions[index], endPositions[index]));
            }
        } else {
            long[] originalStartPositions = computeStartPositions(totalSize, rangeNum);
            long[] startPositions = getCacheStartPositions(filePath, originalStartPositions);
            if (startPositions == null) {
                startPositions = originalStartPositions;
            }
            long currentSize = 0;
            long[] endPositions = computeEndPositions(totalSize, rangeNum);
            for (int index = 0; index < rangeNum; index++) {
                String rangeFilePath = DownloadFileHelper.getRangeFilePath(filePath, index);
                long startPosition = startPositions[index];
                long endPosition = endPositions[index];
                rangeInfoList.add(new RangeInfo(index, rangeFilePath, startPosition, endPosition));
                long rangeSize = startPosition - originalStartPositions[index];
                currentSize += rangeSize;
            }
            taskInfo.setCurrentSize(currentSize);
            taskInfo.setProgress(ProgressHelper.computeProgress(currentSize, taskInfo.getTotalSize()));
        }
        return rangeInfoList;
    }

    private MultiHttpCall getMultiHttpCall(HttpClient client, TaskInfo taskInfo, List<RangeInfo> rangeInfoList) {
        String resKey = taskInfo.getResKey();
        int rangeNum = taskInfo.getRangeNum();
        Map<String, HttpCall> httpCallMap = new HashMap<>();
        for (int index = 0; index < rangeNum; index++) {
            RangeInfo rangeInfo = rangeInfoList.get(index);
            long startPosition = rangeInfo.getStartPosition();
            long endPosition = rangeInfo.getEndPosition();
            if (startPosition < endPosition) {
                String tag = resKey.concat("-").concat(String.valueOf(index));
                httpCallMap.put(tag, client.newCall(tag, taskInfo.getRequestUrl(), startPosition, endPosition));
            }
        }
        return new MultiHttpCall(httpCallMap, rangeInfoList);
    }

    private int computeRangeNum(long curTotalSize) {
        long rangeNum = curTotalSize / 30 * 1024 * 1024;
        if (rangeNum == 0) {
            rangeNum = 1;
        }
        if (rangeNum > 3) {
            rangeNum = 3;
        }
        return (int) rangeNum;
    }

    private Long requestFileInfo(HttpClient client, TaskInfo taskInfo) {
        try {
            HttpResponse httpResponse = client.getHttpResponse(taskInfo.getRequestUrl());
            if (httpResponse.code() == Constants.ResponseCode.OK) {
                fixFilePath(httpResponse, taskInfo);
                return httpResponse.contentLength();
            }
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

    private void fixFilePath(HttpResponse response, TaskInfo taskInfo) {
        String fileDir = taskInfo.getFileDir();
        String filePath = taskInfo.getFilePath();
        if (!TextUtils.isEmpty(filePath)) {
            String contentDisposition = response.header(NetworkHelper.CONTENT_DISPOSITION);
            String contentType = response.header(NetworkHelper.CONTENT_TYPE);
            String fileName = URLUtil.guessFileName(response.url(), contentDisposition, contentType);
            if (TextUtils.isEmpty(fileName)) {
                fileName = DownloadFileHelper.string2MD5(taskInfo.getResKey());
            }
            filePath = fileDir + File.separator + fileName;
            taskInfo.setFilePath(filePath);
        }
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

    private long[] getCacheStartPositions(String filePath, long[] originalStartPositions) {
        RandomAccessFile fileRaf = null;
        try {
            fileRaf = new RandomAccessFile(filePath, "rw");
            long[] startPositions = new long[originalStartPositions.length];
            for (int index = 0; index < originalStartPositions.length; index++) {
                String rangeFilePath = DownloadFileHelper.getRangeFilePath(filePath, index);
                startPositions[index] = readStartPosition(fileRaf, rangeFilePath, originalStartPositions[index]);
            }
            return startPositions;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(fileRaf);
        }
        return null;
    }

    private long readStartPosition(RandomAccessFile fileRaf, String rangeFilePath, long originalStartPosition) {
        RandomAccessFile raf = null;
        try {
            if (!DownloadFileHelper.isFileExists(rangeFilePath)) {
                return 0;
            }
            raf = new RandomAccessFile(rangeFilePath, "rw");
            long startPosition = raf.readLong();
            raf.close();
            startPosition = fixStartPosition(fileRaf, startPosition, originalStartPosition);
            return startPosition;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(raf);
        }
        return 0;
    }

    private long fixStartPosition(RandomAccessFile fileRaf, long startPosition, long originalStartPosition) throws IOException {
        for (; ; ) {
            fileRaf.seek(startPosition);
            if (fileRaf.readByte() == 0) {
                startPosition--;
                if (startPosition <= originalStartPosition) {
                    startPosition = originalStartPosition;
                    break;
                }
            } else {
                break;
            }
        }
        return startPosition;
    }

    public interface HttpCallCreateListener {

        void onCreateFinish(HttpCall call);

    }
}
