package com.hyh.download.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class RangeInfo {

    private int rangeIndex;

    private String tempFilePath;

    private volatile long originalStartPosition;

    private volatile AtomicLong startPosition;

    private volatile long endPosition;

    RangeInfo(int rangeIndex, String tempFilePath, long originalStartPosition, long startPosition, long endPosition) {
        this.rangeIndex = rangeIndex;
        this.tempFilePath = tempFilePath;
        this.originalStartPosition = originalStartPosition;
        this.startPosition = new AtomicLong(startPosition);
        this.endPosition = endPosition;
    }

    public int getRangeIndex() {
        return rangeIndex;
    }

    public String getTempFilePath() {
        return tempFilePath;
    }

    public long getOriginalStartPosition() {
        return originalStartPosition;
    }

    public long getStartPosition() {
        return startPosition.get();
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void addStartPosition(long length) {
        startPosition.addAndGet(length);
    }
}
