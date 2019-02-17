package com.hyh.video.lib;

import android.view.Surface;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IMediaPlayer {

    void changeDataSource(String url);

    void setMediaEventListener(MediaEventListener listener);

    void setMediaProgressListener(MediaProgressListener listener);

    String getDataSource();

    boolean isLooping();

    void setLooping(boolean looping);

    void prepare(boolean autoStart);

    void start();

    void reStart();

    void pause();

    void stop();

    boolean isPlaying();

    void seekTimeTo(int milliSeconds);

    void seekProgressTo(int progress);

    int getCurrentPosition();

    int getDuration();

    void setSurface(Surface surface);

    void setVolume(float leftVolume, float rightVolume);

    void setSpeed(float speed);

    void release();

}
