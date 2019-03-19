package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public class FitXYMeasurer implements ISurfaceMeasurer {

    private final int[] mMeasureSize = new int[2];

    @Override
    public int[] onMeasure(int defaultWidth, int defaultHeight, int videoWidth, int videoHeight) {
        mMeasureSize[0] = defaultWidth;
        mMeasureSize[1] = defaultHeight;
        return mMeasureSize;
    }
}