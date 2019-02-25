package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

class BaseSurfaceMeasurer implements ISurfaceMeasurer {

    private View mSurfaceView;

    private int videoWidth;

    private int videoHeight;

    BaseSurfaceMeasurer(View surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    @Override
    public void setScaleType(HappyVideo.ScaleType scaleType) {

    }

    //1.填充FIT_XY
    //2.居中裁剪：
    @Override
    public int[] onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //ImageView.ScaleType.CENTER

        return null;
    }
}
