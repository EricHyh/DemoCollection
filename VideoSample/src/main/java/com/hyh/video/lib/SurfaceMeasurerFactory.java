package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

class SurfaceMeasurerFactory {

    static ISurfaceMeasurer create(View surfaceView) {
        return new BaseSurfaceMeasurer(surfaceView);
    }
}
