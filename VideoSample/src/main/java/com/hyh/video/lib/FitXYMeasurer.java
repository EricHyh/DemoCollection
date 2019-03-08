package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public class FitXYMeasurer implements ISurfaceMeasurer {

    private final int[] mMeasureSize = new int[2];

    private int mVideoWidth;

    private int mVideoHeight;

    @Override
    public void setVideoWidth(int width, int height) {
        this.mVideoWidth = width;
        this.mVideoHeight = height;
    }

    @Override
    public int[] onMeasure(int maxWidth, int maxHeight) {
        mMeasureSize[0] = maxWidth;
        mMeasureSize[1] = maxHeight;
        return mMeasureSize;
    }
}