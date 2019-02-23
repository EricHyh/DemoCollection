package com.hyh.video.lib;

import android.content.Context;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

public class VideoSurfaceFactory {

    public static IVideoSurface create(Context context) {
        return new HappyTextureView(context);
    }
}
