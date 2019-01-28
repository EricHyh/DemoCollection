package com.hyh.video.lib;

import android.view.Surface;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IMediaPlayer {

    void setVideoSource(VideoSource videoSource);

    boolean isPrepared();

    void prepare(boolean autoStart);

    void start();

    void pause();

    boolean isPlaying();

    void seekTo(int milliSeconds);

    long getCurrentPosition();

    long getDuration();

    void setSurface(Surface surface);

    void setVolume(float leftVolume, float rightVolume);

    void setSpeed(float speed);

    void release();

}
