package com.hyh.video.lib;

/**
 * Created by Eric_He on 2019/2/16.
 */

public interface MediaProgressListener {

    void onMediaProgress(int progress, long currentPosition, long duration);

}
