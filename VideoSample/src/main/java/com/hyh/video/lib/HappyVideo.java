package com.hyh.video.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public class HappyVideo extends FrameLayout {

    private final IMediaPlayer mMediaPlayer = new MediaSystem();
    private final MediaEventListener mMediaEventListener = new InnerMediaEventListener();
    private final MediaProgressListener mMediaProgressListener = new InnerMediaProgressListener();
    private final List<MediaEventListener> mMediaEventListeners = new ArrayList<>();
    private final List<MediaProgressListener> mMediaProgressListeners = new ArrayList<>();

    private IVideoBackground mVideoBackground;
    private IVideoSurface mVideoSurface;
    private IVideoPreview mVideoPreview;
    private IVideoController mVideoController;


    public HappyVideo(@NonNull Context context) {
        this(context, null);
    }

    public HappyVideo(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HappyVideo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMediaPlayer.setMediaEventListener(mMediaEventListener);
        mMediaPlayer.setMediaProgressListener(mMediaProgressListener);

        this.mVideoSurface = VideoSurfaceFactory.create(context);
        this.mVideoController = new DefaultVideoController(context);
    }

    public void setVideoBackground(IVideoBackground background) {
        if (mVideoBackground == background) return;
        if (mVideoBackground != null) {
            removeView(mVideoBackground.getView());
        }
        this.mVideoBackground = background;
        if (mVideoBackground != null) {
            addView(mVideoBackground.getView(), 0);
        }
    }

    public void setVideoController(IVideoController controller) {
        if (mVideoController == controller) return;
        if (mVideoController != null) {
            removeView(mVideoController.getView());
        }
        this.mVideoController = controller;
        if (mVideoController != null) {
            addView(mVideoController.getView());
        }
    }

    public void addMediaEventListener(MediaEventListener listener) {
        mMediaEventListeners.add(listener);
    }

    public void removeMediaEventListener(MediaEventListener listener) {
        mMediaEventListeners.remove(listener);
    }

    public void addMediaProgressListener(MediaProgressListener listener) {
        mMediaProgressListeners.add(listener);
    }

    public void removeMediaProgressListener(MediaProgressListener listener) {
        mMediaProgressListeners.remove(listener);
    }

    public boolean setDataSource(String source) {
        return mMediaPlayer.setDataSource(source);
    }

    public String getDataSource() {
        return mMediaPlayer.getDataSource();
    }

    public void setLooping(boolean looping) {
        mMediaPlayer.setLooping(looping);
    }

    public boolean isLooping() {
        return mMediaPlayer.isLooping();
    }

    public void prepare(boolean autoStart) {
        mMediaPlayer.prepare(autoStart);
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void reStart() {
        mMediaPlayer.reStart();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void seekTimeTo(int milliSeconds) {
        mMediaPlayer.seekTimeTo(milliSeconds);
    }

    public void seekProgressTo(int progress) {
        mMediaPlayer.seekProgressTo(progress);
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public boolean isSupportSpeed() {
        return mMediaPlayer.isSupportSpeed();
    }

    public void setSpeed(float speed) {
        mMediaPlayer.setSpeed(speed);
    }

    public void release() {
        mMediaPlayer.release();
    }

    public void setVideoScaleType(ScaleType scaleType) {
        mVideoSurface.setScaleType(scaleType);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
    }

    private class InnerMediaEventListener implements MediaEventListener {

        @Override
        public void onPreparing() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPreparing();
            }
        }

        @Override
        public void onPrepared() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPrepared();
            }
        }

        @Override
        public void onStart(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onStart(currentPosition, duration);
            }
        }

        @Override
        public void onPlaying(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPlaying(currentPosition, duration);
            }
        }

        @Override
        public void onPause(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPause(currentPosition, duration);
            }
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onStop(currentPosition, duration);
            }
        }

        @Override
        public void onBufferingStart() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onBufferingStart();
            }
        }

        @Override
        public void onBufferingEnd() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onBufferingEnd();
            }
        }

        @Override
        public void onBufferingUpdate(int progress) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onBufferingUpdate(progress);
            }
        }

        @Override
        public void onSeekStart(int seekMilliSeconds, long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onSeekStart(seekMilliSeconds, currentPosition, duration);
            }
        }

        @Override
        public void onSeekEnd(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onSeekEnd(currentPosition, duration);
            }
        }

        @Override
        public void onError(int what, int extra) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onError(what, extra);
            }
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onVideoSizeChanged(width, height);
            }
        }

        @Override
        public void onCompletion() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onCompletion();
            }
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onRelease(currentPosition, duration);
            }
        }
    }

    private class InnerMediaProgressListener implements MediaProgressListener {

        @Override
        public void onMediaProgress(int progress, long currentPosition, long duration) {
            for (MediaProgressListener listener : mMediaProgressListeners) {
                listener.onMediaProgress(progress, currentPosition, duration);
            }
        }
    }


    public enum ScaleType {
        FIT_XY,
        FIT_CENTER,
        CENTER_CROP,
        CENTER_INSIDE;
    }
}
