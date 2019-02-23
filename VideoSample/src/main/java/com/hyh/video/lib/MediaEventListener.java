package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface MediaEventListener {

    void onPreparing();

    void onPrepared();

    void onStart(long currentPosition, long duration);

    void onPause(long currentPosition, long duration);

    void onStop(long currentPosition, long duration);

    void onBufferingUpdate(int progress);

    void onSeekStart(int seekMilliSeconds, long currentPosition, long duration);

    void onSeekComplete(long currentPosition, long duration);

    void onError(int what, int extra);

    void onVideoSizeChanged(int width, int height);

    void onCompletion();

    void onRelease(long currentPosition, long duration);

}
