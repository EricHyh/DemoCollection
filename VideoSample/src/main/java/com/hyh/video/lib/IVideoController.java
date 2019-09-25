package com.hyh.video.lib;

import android.view.View;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IVideoController extends IVideoSurface.SurfaceListener {

    View getView();

    void setup(VideoDelegate videoDelegate, CharSequence title, long playCount, IMediaInfo mediaInfo);

    boolean interceptPrepare(boolean autoStart);

    boolean interceptStart();

    boolean interceptRestart();

    boolean interceptRetry();

    void onVideoSceneChanged(FrameLayout videoContainer, int scene);

    boolean isFullScreenLocked();

    void onBackPress();

}