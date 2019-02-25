package com.hyh.video.lib;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IVideoController extends MediaEventListener, MediaProgressListener {

    View getView();

    void setUp(IMediaPlayer mediaPlayer);

    void setTitle(CharSequence text);

}