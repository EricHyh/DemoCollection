package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/3/8
 */

public class FitCenterMeasurer implements ISurfaceMeasurer {

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
        if (maxWidth == 0 || maxHeight == 0 || mVideoWidth == 0 || mVideoHeight == 0) {
            mMeasureSize[0] = maxWidth;
            mMeasureSize[1] = maxHeight;
            return mMeasureSize;
        }
        float ratio = maxHeight * 1.0f / maxWidth;
        float videoRatio = mVideoHeight * 1.0f / mVideoWidth;
        if (videoRatio > ratio) {
            mMeasureSize[1] = maxHeight;
            mMeasureSize[0] = Math.round(mMeasureSize[1] / videoRatio);
        } else {
            mMeasureSize[0] = maxWidth;
            mMeasureSize[1] = Math.round(mMeasureSize[0] * videoRatio);
        }
        return mMeasureSize;
    }
}