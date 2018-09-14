package com.hyh.tools.download.utils;

import android.content.Context;

import com.hyh.tools.download.api.FileRequest;
import com.hyh.tools.download.db.bean.TaskDBInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Administrator
 * @description
 * @data 2018/2/26
 */

public class FD_MultiUtil {

    public static int computeMultiThreadNum(int maxSynchronousDownloadNum) {
        return 3;
    }

    public static Object[] getCurrentSizeAndMultiPositions(Context context, FileRequest request, File file, TaskDBInfo historyInfo) {
        Object[] objects = new Object[3];
        objects[0] = 0L;
        objects[1] = null;
        objects[2] = null;
        if (request.byMultiThread()) {
            if (historyInfo != null) {
                Integer rangeNum = historyInfo.getRangeNum();
                if (rangeNum == null || rangeNum == 1) {
                    request.setByMultiThread(false);
                    objects[0] = file.length();
                    return objects;
                } else {
                    long currentSize = 0;
                    long[] startPositions = new long[rangeNum];
                    Long totalSize = historyInfo.getTotalSize();
                    long[] endPositions = computeEndPositions(totalSize, rangeNum);

                    RandomAccessFile fileRaf = null;
                    try {
                        fileRaf = new RandomAccessFile(file, "rw");

                        long partSize = totalSize / rangeNum;

                        for (int rangeId = 0; rangeId < rangeNum; rangeId++) {
                            File tempFile = FD_FileUtil.getTempFile(context, request.key(), rangeId);

                            RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
                            long l = raf.readLong();
                            raf.close();
                            long originalStartPosition;
                            if (rangeId == 0) {
                                originalStartPosition = 0;
                            } else {
                                originalStartPosition = partSize * rangeId + 1;
                            }
                            long rangeSize = getRangeSize(fileRaf, l, originalStartPosition, endPositions[rangeId], 2);
                            if (rangeId == 0) {
                                currentSize += rangeSize;
                            } else {
                                currentSize += (rangeSize - (partSize * rangeId + 1));
                            }
                            startPositions[rangeId] = rangeSize;
                        }
                        objects[0] = currentSize;
                        objects[1] = startPositions;
                        objects[2] = endPositions;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        FD_StreamUtil.close(fileRaf);
                    }
                    return objects;
                }
            } else {
                FD_FileUtil.deleteDownloadFile(context, request.key(), 1);
            }
        } else {
            if (historyInfo != null) {
                Integer rangeNum = historyInfo.getRangeNum();
                if (rangeNum == null || rangeNum == 1) {
                    objects[0] = file.length();
                    return objects;
                } else {
                    request.setByMultiThread(true);
                    return getCurrentSizeAndMultiPositions(context, request, file, historyInfo);
                }
            } else {
                FD_FileUtil.deleteDownloadFile(context, request.key(), 1);
            }
        }
        return objects;
    }


    private static long[] computeEndPositions(long totalSize, int rangeNum) {
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

    private static long getRangeSize(RandomAccessFile fileRaf, long l, long originalStartPosition, long endPosition, int blink) throws IOException {
        if (l == endPosition + 1) {
            return l;
        }
        if (l <= originalStartPosition) {
            return originalStartPosition;
        }
        fileRaf.seek(l);
        if (fileRaf.readByte() == 0) {
            l -= blink;
            blink *= 2;
            return getRangeSize(fileRaf, l, originalStartPosition, endPosition, blink);
        } else {
            return l;
        }
    }
}
