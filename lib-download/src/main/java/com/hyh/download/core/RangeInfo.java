package com.hyh.download.core;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class RangeInfo {

    private int rangeId;

    private String rangeFilePath;

    private volatile long startPosition;

    private volatile long endPosition;

    RangeInfo(int rangeId, String rangeFilePath, long startPosition, long endPosition) {
        this.rangeId = rangeId;
        this.rangeFilePath = rangeFilePath;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public int getRangeId() {
        return rangeId;
    }

    public void setRangeId(int rangeId) {
        this.rangeId = rangeId;
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
