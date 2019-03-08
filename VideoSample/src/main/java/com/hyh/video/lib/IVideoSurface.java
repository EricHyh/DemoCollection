package com.hyh.video.lib;

import android.view.Surface;
import android.view.View;

/**
 * @author Administrator
 * @description 负责视频播放区域的绘制
 * @data 2019/2/23
 */
public interface IVideoSurface {

    View getView();

    void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer);

    void setSurfaceListener(SurfaceListener listener);

    interface SurfaceListener {

        void onSurfaceCreate(Surface surface);

        void onSurfaceSizeChanged(Surface surface, int width, int height);

        void onSurfaceDestroyed(Surface surface);
    }
}
