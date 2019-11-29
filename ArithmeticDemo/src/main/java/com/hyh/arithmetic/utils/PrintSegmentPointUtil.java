package com.hyh.arithmetic.utils;

import android.util.Log;

/**
 * @author Administrator
 * @description
 * @data 2019/11/29
 */
public class PrintSegmentPointUtil {
    private static final String TAG = "PrintSegmentPointUtil";

    public void printSegmentPoint(int[][] points, int segmentLength) {
        int segmentIndex = 0;
        int segmentPointX;
        int segmentPointY;

        int length = 0;


        int[] lastPoint = points[0];
        for (int index = 1; index <= points.length; index++) {
            int[] point;
            if (index == points.length) {
                point = points[0];
            } else {
                point = points[index];
            }

            boolean x_axis;
            int diff;
            if (lastPoint[0] == point[0]) {
                diff = point[1] - lastPoint[1];
                x_axis = false;
            } else {
                diff = point[0] - lastPoint[0];
                x_axis = true;
            }
            length += Math.abs(diff);
            if (length == segmentLength) {
                segmentPointX = point[0];
                segmentPointY = point[1];
                length = 0;
                Log.d(TAG, "printSegmentPoint: segmentIndex: " + segmentIndex + ", segmentPointX: " + segmentPointX + ", segmentPointY: " + segmentPointY);
                segmentIndex++;
            } else if (length > segmentLength) {
                while (length >= segmentLength) {
                    int dl = length - segmentLength;
                    if (x_axis) {
                        segmentPointY = point[1];
                        if (diff > 0) {
                            segmentPointX = point[0] - dl;
                        } else {
                            segmentPointX = point[0] + dl;
                        }
                    } else {
                        segmentPointX = point[0];
                        if (diff > 0) {
                            segmentPointY = point[1] - dl;
                        } else {
                            segmentPointY = point[1] + dl;
                        }
                    }
                    length = dl;
                    Log.d(TAG, "printSegmentPoint: segmentIndex: " + segmentIndex + ", segmentPointX: " + segmentPointX + ", segmentPointY: " + segmentPointY);
                    segmentIndex++;
                }
            }
            lastPoint = point;
        }
    }
}
