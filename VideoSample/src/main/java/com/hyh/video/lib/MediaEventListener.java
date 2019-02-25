package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface MediaEventListener {

    void onPreparing();

    void onPrepared(int duration);

    void onStart(int currentPosition, int duration);

    void onPlaying(int currentPosition, int duration);

    void onPause(int currentPosition, int duration);

    void onStop(int currentPosition, int duration);

    void onBufferingStart();

    void onBufferingEnd();

    void onBufferingUpdate(int progress);

    void onSeekStart(int seekMilliSeconds, int seekProgress);

    void onSeekEnd();

    void onError(int what, int extra);

    void onVideoSizeChanged(int width, int height);

    void onCompletion();

    void onRelease(int currentPosition, int duration);

}
