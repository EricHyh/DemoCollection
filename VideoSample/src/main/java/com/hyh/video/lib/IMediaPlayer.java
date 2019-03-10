package com.hyh.video.lib;

import android.view.Surface;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public interface IMediaPlayer {

    boolean setDataSource(DataSource source);

    void setMediaEventListener(MediaEventListener listener);

    void setMediaProgressListener(MediaProgressListener listener);

    DataSource getDataSource();

    boolean isLooping();

    void setLooping(boolean looping);

    int getMediaState();

    void prepare(boolean autoStart);

    void start();

    void restart();

    void retry();

    void pause();

    void stop();

    boolean isExecuteStart();

    boolean isPlaying();

    void seekTimeTo(int millis);

    void seekProgressTo(int progress);

    long getCurrentPosition();

    long getDuration();

    void setSurface(Surface surface);

    void setVolume(float leftVolume, float rightVolume);

    boolean isSupportSpeed();

    void setSpeed(float speed);

    boolean isReleased();

    void release();

    class State {

        public static final int IDLE = 0;

        public static final int INITIALIZED = 1;

        public static final int PREPARING = 2;

        public static final int PREPARED = 3;

        public static final int STARTED = 4;

        public static final int PAUSED = 5;

        public static final int STOPPED = 6;

        public static final int COMPLETED = 7;

        public static final int ERROR = 8;

        public static final int END = 9;

    }
}
