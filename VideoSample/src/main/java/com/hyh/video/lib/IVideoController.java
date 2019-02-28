package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IVideoController {

    View getView();

    void setUp(HappyVideo happyVideo);

    void setTitle(CharSequence text);

    boolean interceptPrepare(boolean autoStart);

    boolean interceptStart();

    boolean interceptReStart();

    boolean interceptRetry();

    void onSurfaceCreate();

    void onSurfaceDestroyed();

}