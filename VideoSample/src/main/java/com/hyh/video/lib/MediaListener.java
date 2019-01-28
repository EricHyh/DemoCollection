package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface MediaListener {

    void onPrepared();

    void onBufferingUpdate(int progress);

    void onSeekComplete();

    void onError(int what, int extra);

    void onVideoSizeChanged(int width, int height);

    void onCompletion();
}
