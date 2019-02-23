package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

class BaseSurfaceMeasurer implements ISurfaceMeasurer {

    private View mSurfaceView;

    BaseSurfaceMeasurer(View surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    }
}
