package com.hyh.video.lib;

import android.view.Surface;

/**
 * Created by Eric_He on 2019/2/16.
 */

public interface IVideoSurface {

    void setSurfaceListener(OnSurfaceListener listener);

    interface OnSurfaceListener {

        void onSurfaceCreate(Surface surface);

        void onSurfaceSizeChanged(Surface surface, int width, int height);

        void onSurfaceDestroyed(Surface surface);
    }
}
