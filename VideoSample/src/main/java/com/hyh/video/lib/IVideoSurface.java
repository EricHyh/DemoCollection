package com.hyh.video.lib;

import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description 负责视频播放区域的绘制
 * @data 2019/2/23
 */
public interface IVideoSurface {

    View getView();

    void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer);

    void setVideoSize(int videoWidth, int videoHeight);

    void setSurfaceListener(SurfaceListener listener);

    void reset();

    boolean isSupportRotate();

    void onVideoSceneChanged(FrameLayout videoContainer, int scene);

    interface SurfaceListener {

        void onSurfaceCreate(Surface surface);

        void onSurfaceSizeChanged(Surface surface, int width, int height);

        void onSurfaceDestroyed(Surface surface);
    }
}
