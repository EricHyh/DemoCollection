package com.hyh.download.core;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class RangeInfo {

    private int rangeIndex;

    private String rangeFilePath;

    private volatile long startPosition;

    private volatile long endPosition;

    RangeInfo(int rangeIndex, String rangeFilePath, long startPosition, long endPosition) {
        this.rangeIndex = rangeIndex;
        this.rangeFilePath = rangeFilePath;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public int getRangeIndex() {
        return rangeIndex;
    }

    public void setRangeIndex(int rangeIndex) {
        this.rangeIndex = rangeIndex;
    }

    public String getRangeFilePath() {
        return rangeFilePath;
    }

    public void setRangeFilePath(String rangeFilePath) {
        this.rangeFilePath = rangeFilePath;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }


    public void addStartPosition(long length) {
        startPosition += length;
    }
}
