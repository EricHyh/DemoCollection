package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface MediaEventListener {

    void onInitialized();

    void onPreparing(boolean autoStart);

    void onPrepared(long duration);

    void onExecuteStart();

    void onStart(long currentPosition, long duration);

    void onPlaying(long currentPosition, long duration);

    void onPause(long currentPosition, long duration);

    void onStop(long currentPosition, long duration);

    void onBufferingStart();

    void onBufferingEnd();

    void onBufferingUpdate(int progress);

    void onSeekStart(long seekMilliSeconds, int seekProgress);

    void onSeekEnd();

    void onError(int what, int extra);

    void onVideoSizeChanged(int width, int height);

    void onCompletion();

    void onRelease(long currentPosition, long duration);

}
