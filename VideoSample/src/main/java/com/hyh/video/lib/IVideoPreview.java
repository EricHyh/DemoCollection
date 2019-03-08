package com.hyh.video.lib;

import android.view.View;

/**
 * Created by Eric_He on 2019/2/24.
 */

public interface IVideoPreview {

    View getView();

    void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer);

    void setUp(HappyVideo happyVideo, IMediaInfo mediaInfo);

}