package com.hyh.video.lib;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

class BaseSurfaceMeasurer implements ISurfaceMeasurer {

    private final View mSurfaceView;

    private HappyVideo.ScaleType mScaleType = HappyVideo.ScaleType.FIT_CENTER;

    private int mVideoWidth;

    private int mVideoHeight;

    private int[] mMeasureSize = new int[2];

    BaseSurfaceMeasurer(View surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    @Override
    public void setScaleType(HappyVideo.ScaleType scaleType) {
        this.mScaleType = scaleType;
    }

    @Override
    public void setVideoWidth(int width, int height) {
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        mSurfaceView.requestLayout();
    }

    @Override
    public int[] onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewParent parent = mSurfaceView.getParent();
        if (parent == null || !(parent instanceof ViewGroup)) {
            mMeasureSize[0] = View.getDefaultSize(0, widthMeasureSpec);
            mMeasureSize[1] = View.getDefaultSize(0, heightMeasureSpec);
            return mMeasureSize;
        }
        ViewGroup viewGroup = (ViewGroup) parent;
        int parentWidth = viewGroup.getMeasuredWidth();
        int parentHeight = viewGroup.getMeasuredHeight();
        if (parentWidth == 0 || parentHeight == 0 || mVideoWidth == 0 || mVideoHeight == 0) {
            mMeasureSize[0] = parentWidth;
            mMeasureSize[1] = parentHeight;
            return mMeasureSize;
        }
        switch (mScaleType) {
            case FIT_XY: {
                mMeasureSize[0] = parentWidth;
                mMeasureSize[1] = parentHeight;
                break;
            }
            case FIT_CENTER: {
                float parentRatio = parentHeight * 1.0f / parentWidth;
                float videoRatio = mVideoHeight * 1.0f / mVideoWidth;
                if (videoRatio > parentRatio) {
                    mMeasureSize[1] = parentHeight;
                    mMeasureSize[0] = Math.round(mMeasureSize[1] / videoRatio);
                } else {
                    mMeasureSize[0] = parentWidth;
                    mMeasureSize[1] = Math.round(mMeasureSize[0] * videoRatio);
                }
                break;
            }
            case CENTER_INSIDE: {
                float parentRatio = parentHeight * 1.0f / parentWidth;
                float videoRatio = mVideoHeight * 1.0f / mVideoWidth;
                if (videoRatio > parentRatio) {
                    if (mVideoHeight > parentHeight) {
                        mMeasureSize[1] = parentHeight;
                    } else {
                        mMeasureSize[1] = mVideoHeight;
                    }
                    mMeasureSize[0] = Math.round(mMeasureSize[1] / videoRatio);
                } else {
                    if (mVideoWidth > parentWidth) {
                        mMeasureSize[0] = parentWidth;
                    } else {
                        mMeasureSize[0] = mVideoWidth;
                    }
                    mMeasureSize[1] = Math.round(mMeasureSize[0] * videoRatio);
                }
                break;
            }
        }
        return mMeasureSize;
    }
}
