package com.hyh.download.utils;

/**
 * @author Administrator
 * @description
 * @data 2018/12/20
 */

public class ProgressHelper {

    public static int computeProgress(long currentSize, long totalSize) {
        if (totalSize <= 0) {
            return -1;
        }
        return Math.round(currentSize * 100.0f / totalSize);
    }
}
