package com.hyh.arithmetic.utils;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/11/28
 */
public class SortUtil {

    //快速排序实现，非递归实现
    public static void sort(List<Integer> list) {
        int[][] rangeArray = new int[][]{{0, list.size() - 1}};
        int rangeSize = 1;
        while (rangeSize > 0) {
            int newRangeMaxSize = rangeSize * 2;
            int newRangeSize = 0;
            int[][] newRangeArray;
            if (rangeArray.length >= newRangeMaxSize) {
                newRangeArray = rangeArray;
            } else {
                newRangeArray = new int[2][newRangeMaxSize];
            }

            for (int index = 0; index < rangeSize; index++) {
                int[] range = rangeArray[index];
                int startIndex = range[0];
                int endIndex = range[1];
                int split = split(list, startIndex, endIndex);
                int leftRangeStartIndex = startIndex;
                int leftRangeEndIndex = split - 1;
                if (leftRangeStartIndex < leftRangeEndIndex) {
                    newRangeArray[newRangeSize] = new int[]{leftRangeStartIndex, leftRangeEndIndex};
                    newRangeSize++;
                }
                int rightRangeStartIndex = split + 1;
                int rightRangeEndIndex = endIndex;
                if (rightRangeStartIndex < rightRangeEndIndex) {
                    newRangeArray[newRangeSize] = new int[]{rightRangeStartIndex, rightRangeEndIndex};
                    newRangeSize++;
                }
            }
            rangeSize = newRangeSize;
            rangeArray = newRangeArray;
        }
    }

    //快速排序实现，递归实现
    public static void sortRange(List<Integer> list, int startIndex, int endIndex) {
        int split = split(list, startIndex, endIndex);
        int leftRangeStartIndex = startIndex;
        int leftRangeEndIndex = split - 1;
        if (leftRangeStartIndex < leftRangeEndIndex) {
            sortRange(list, leftRangeStartIndex, leftRangeEndIndex);
        }

        int rightRangeStartIndex = split + 1;
        int rightRangeEndIndex = endIndex;
        if (rightRangeStartIndex < rightRangeEndIndex) {
            sortRange(list, rightRangeStartIndex, rightRangeEndIndex);
        }
    }

    static int split(List<Integer> list, int startIndex, int endIndex) {
        int baseIndex = startIndex;
        int baseValue = list.get(startIndex);
        int i = startIndex + 1;
        int j = endIndex;
        boolean ltr = false;
        while (i <= j) {
            if (ltr) {
                Integer value = list.get(i);
                if (value > baseValue) {
                    list.set(baseIndex, value);
                    list.set(i, baseValue);
                    baseIndex = i;
                    ltr = false;
                }
                i++;
            } else {
                Integer value = list.get(j);
                if (value < baseValue) {
                    list.set(baseIndex, value);
                    list.set(j, baseValue);
                    baseIndex = j;
                    ltr = true;
                }
                j--;
            }
        }
        return baseIndex;
    }
}