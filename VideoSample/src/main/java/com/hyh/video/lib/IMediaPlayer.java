package com.hyh.video.lib;

import android.view.Surface;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IMediaPlayer {

    void setMediaListener(MediaListener listener);

    boolean isLooping();

    void setLooping(boolean looping);

    boolean isPrepared();

    void prepare(boolean autoStart);

    void start();

    void reStart();

    void pause();

    boolean isPlaying();

    void seekTo(int milliSeconds);

    int getCurrentPosition();

    int getDuration();

    void setSurface(Surface surface);

    void setVolume(float leftVolume, float rightVolume);

    void setSpeed(float speed);

    void release();

}
