package com.hyh.download.utils;

import android.text.TextUtils;

import com.hyh.download.core.RangeInfo;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * @author Administrator
 * @description
 * @data 2018/12/29
 */

public class RangeUtil {


    public static long[] computeStartPositions(long totalSize, int rangeNum) {
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

    public static long[] computeEndPositions(long totalSize, int rangeNum) {
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

    public static long[] getCacheStartPositions(String filePath, long[] originalStartPositions, long[] endPositions) {
        String tempFilePath = DownloadFileHelper.getTempFilePath(filePath);
        long[] startPositions = RangeUtil.readStartPositions(tempFilePath, originalStartPositions.length);
        return fixStartPositions(filePath, startPositions, originalStartPositions, endPositions);
    }

    private static long[] fixStartPositions(String filePath, long[] startPositions, long[] originalStartPositions, long[] endPositions) {
        RandomAccessFile fileRaf = null;
        try {
            fileRaf = new RandomAccessFile(filePath, "rw");
            for (int index = 0; index < startPositions.length; index++) {
                startPositions[index] = fixStartPosition(fileRaf, startPositions[index], originalStartPositions[index], endPositions[index]);
            }
            return startPositions;
        } catch (Exception e) {
            return originalStartPositions;
        } finally {
            StreamUtil.close(fileRaf);
        }
    }


    public static long fixStartPosition(String filePath, long oldStartPosition, long originalStartPosition, long endPosition) {
        if (oldStartPosition <= originalStartPosition) {
            return originalStartPosition;
        }
        if (oldStartPosition > endPosition + 1) {
            oldStartPosition = endPosition + 1;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filePath, "rw");
            for (; ; ) {
                raf.seek(oldStartPosition - 1);
                if (raf.read() <= 0) {
                    oldStartPosition--;
                    if (oldStartPosition <= originalStartPosition) {
                        oldStartPosition = originalStartPosition;
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            oldStartPosition = originalStartPosition;
        } finally {
            StreamUtil.close(raf);
        }
        return oldStartPosition;
    }


    private static long fixStartPosition(RandomAccessFile fileRaf, long oldStartPosition, long originalStartPosition, long endPosition) {
        if (oldStartPosition <= originalStartPosition) {
            return originalStartPosition;
        }
        if (oldStartPosition > endPosition + 1) {
            oldStartPosition = endPosition + 1;
        }
        try {
            for (; ; ) {
                fileRaf.seek(oldStartPosition - 1);
                if (fileRaf.read() <= 0) {
                    oldStartPosition--;
                    if (oldStartPosition <= originalStartPosition) {
                        oldStartPosition = originalStartPosition;
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            oldStartPosition = originalStartPosition;
        }
        return oldStartPosition;
    }

    private static long[] readStartPositions(String tempFilePath, int length) {
        long[] startPositions = new long[length];
        RandomAccessFile tempFileRaf = null;
        try {
            tempFileRaf = new RandomAccessFile(tempFilePath, "rw");
            for (int index = 0; index < length; index++) {
                tempFileRaf.seek(index * 8);
                startPositions[index] = tempFileRaf.readLong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtil.close(tempFileRaf);
        }
        return startPositions;
    }

    public static int computeRangeNum(long curTotalSize) {
        if (curTotalSize <= 0) {
            return 1;
        }
        long rangeNum = curTotalSize / (30 * 1024 * 1024);
        if (rangeNum <= 0) {
            rangeNum = 1;
        }
        if (rangeNum > 3) {
            rangeNum = 3;
        }
        rangeNum = 3;
        return (int) rangeNum;
    }

    public static int computeProgress(long currentSize, long totalSize) {
        if (totalSize <= 0) {
            return -1;
        }
        return Math.round(currentSize * 100.0f / totalSize);
    }

    public static void writeStartPosition(RangeInfo rangeInfo) {
        String tempFilePath = rangeInfo.getTempFilePath();
        if (TextUtils.isEmpty(tempFilePath)) {
            return;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tempFilePath, "rws");
            raf.seek(rangeInfo.getRangeIndex() * 8);
            raf.writeLong(rangeInfo.getStartPosition());
            raf.close();
        } catch (Exception e) {
            StreamUtil.close(raf);
        }
    }
}
