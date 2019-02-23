package com.hyh.video.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public class HappyVideo extends FrameLayout {

    private final IMediaPlayer mMediaPlayer = new MediaSystem();
    private final IVideoSurface mVideoSurface;
    private final IVideoController mVideoController;

    public HappyVideo(@NonNull Context context) {
        this(context, null);
    }

    public HappyVideo(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HappyVideo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mVideoSurface = VideoSurfaceFactory.create(context);
        this.mVideoController = new DefaultVideoController(context);


    }
}
