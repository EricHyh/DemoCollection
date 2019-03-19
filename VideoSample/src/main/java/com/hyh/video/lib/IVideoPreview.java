package com.hyh.video.lib;

import android.view.View;

/**
 * Created by Eric_He on 2019/2/24.
 */

public interface IVideoPreview extends TextureSurface.SurfaceListener {

    View getView();

    void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer);

    void setUp(VideoDelegate videoDelegate, IMediaInfo mediaInfo);

}