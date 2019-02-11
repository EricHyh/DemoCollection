package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface MediaListener {

    void onPreparing();

    void onPrepared();

    void onStart(long currentPosition);

    void onPause(long currentPosition);

    void onBufferingUpdate(int progress);

    void onSeekStart(int milliSeconds);

    void onSeekComplete();

    void onError(int what, int extra);

    void onVideoSizeChanged(int width, int height);

    void onCompletion();
}
