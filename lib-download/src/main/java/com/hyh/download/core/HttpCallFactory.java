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
                fixFilePath(response, taskInfo);
                taskInfo.setCacheRequestUrl(taskInfo.getRequestUrl());
                taskInfo.setCacheTargetUrl(response.url());

                long curTotalSize = response.contentLength();
                rangeNum = computeRangeNum(curTotalSize);
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
                taskInfo.setProgress(ProgressHelper.computeProgress(fileLength, taskInfo.getTotalSize()));
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
                fixFilePath(response, taskInfo);
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
                    taskInfo.setCurrentSize(0);
                    taskInfo.setProgress(0);
                    taskInfo.setETag(response.header(NetworkHelper.ETAG));
                    if (curTotalSize > 0) {
                        //重新计算下载区间数
                        rangeNum = computeRangeNum(curTotalSize);
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
            return client.newCall(resKey, url, taskInfo.getCurrentSize());
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
                String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
                rangeInfoList.add(new RangeInfo(index, tempFilePath, startPositions[index], startPositions[index], endPositions[index]));
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
                String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
                long startPosition = startPositions[index];
                long endPosition = endPositions[index];
                rangeInfoList.add(new RangeInfo(index, tempFilePath, originalStartPositions[index], startPosition, endPosition));
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
        Map<String, HttpCall> httpCallMap = new HashMap<>();
        for (RangeInfo rangeInfo : rangeInfoList) {
            long startPosition = rangeInfo.getStartPosition();
            long endPosition = rangeInfo.getEndPosition();
            if (startPosition < endPosition) {
                String tag = resKey.concat("-").concat(String.valueOf(rangeInfo.getRangeIndex()));
                httpCallMap.put(tag, client.newCall(tag, taskInfo.getRequestUrl(), startPosition, endPosition));
            }
        }
        return new MultiHttpCall(httpCallMap, rangeInfoList);
    }

    private int computeRangeNum(long curTotalSize) {
        long rangeNum = curTotalSize / 30 * 1024 * 1024;
        if (rangeNum <= 0) {
            rangeNum = 1;
        }
        if (rangeNum > 3) {
            rangeNum = 3;
        }
        return (int) rangeNum;
    }

    private HttpResponse requestFileInfo(HttpClient client, TaskInfo taskInfo) {
        try {
            HttpResponse response = client.getHttpResponse(taskInfo.getRequestUrl());
            return response;
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
                String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
                startPositions[index] = readStartPosition(fileRaf, tempFilePath, index, originalStartPositions[index]);
            }
            return startPositions;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(fileRaf);
        }
        return null;
    }

    private long readStartPosition(RandomAccessFile fileRaf, String rangeFilePath, int index, long originalStartPosition) {
        RandomAccessFile raf = null;
        try {
            if (!DownloadFileHelper.isFileExists(rangeFilePath)) {
                return 0;
            }
            raf = new RandomAccessFile(rangeFilePath, "rw");
            raf.seek(index * 8);
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
