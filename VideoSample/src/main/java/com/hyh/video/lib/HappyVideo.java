package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.ViewGroup;
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
    private final IVideoSurface.SurfaceListener mSurfaceListener = new InnerSurfaceListener();
    private final MediaEventListener mMediaEventListener = new InnerMediaEventListener();
    private final MediaProgressListener mMediaProgressListener = new InnerMediaProgressListener();
    private final List<MediaEventListener> mMediaEventListeners = new ArrayList<>();
    private final List<MediaProgressListener> mMediaProgressListeners = new ArrayList<>();
    private final List<IVideoSurface.SurfaceListener> mSurfaceListeners = new ArrayList<>();

    private IVideoBackground mVideoBackground;
    private IVideoSurface mVideoSurface;
    private IVideoPreview mVideoPreview;
    private IVideoController mVideoController;

    private CharSequence mTitle;


    public HappyVideo(@NonNull Context context) {
        this(context, null);
    }

    public HappyVideo(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HappyVideo(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.BLACK);
        mMediaPlayer.setMediaEventListener(mMediaEventListener);
        mMediaPlayer.setMediaProgressListener(mMediaProgressListener);

        this.mVideoSurface = VideoSurfaceFactory.create(context);
        this.mVideoController = new DefaultVideoController(context);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        addView(mVideoSurface.getView(), params);
        //addView(mVideoPreview.getView());
        //addView(mVideoController.getView());
        mVideoSurface.setSurfaceListener(mSurfaceListener);
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


    public void setVideoPreview(IVideoPreview videoPreview) {
        if (mVideoPreview == videoPreview) return;
        if (mVideoPreview != null) {
            removeView(mVideoPreview.getView());
        }
        this.mVideoPreview = videoPreview;
        if (mVideoPreview != null) {
            if (mVideoBackground != null) {
                addView(mVideoPreview.getView(), 2);
            } else {
                addView(mVideoPreview.getView(), 1);
            }
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
            mVideoController.setUp(this, this.mTitle);
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

    public void addSurfaceListener(IVideoSurface.SurfaceListener listener) {
        mSurfaceListeners.add(listener);
    }

    public void removeSurfaceListener(IVideoSurface.SurfaceListener listener) {
        mSurfaceListeners.remove(listener);
    }

    public boolean setup(DataSource source, CharSequence title, boolean looping) {
        boolean set = mMediaPlayer.setDataSource(source);
        if (set) {
            this.mTitle = title;
            mMediaPlayer.setLooping(looping);
            if (mVideoController != null) {
                mVideoController.setUp(this, title);
            }
        }
        return set;
    }

    public int getMediaState() {
        return mMediaPlayer.getMediaState();
    }

    public DataSource getDataSource() {
        return mMediaPlayer.getDataSource();
    }

    public void setLooping(boolean looping) {
        mMediaPlayer.setLooping(looping);
    }

    public boolean isLooping() {
        return mMediaPlayer.isLooping();
    }

    public void prepare(boolean autoStart) {
        if (mVideoController != null && mVideoController.interceptPrepare(autoStart)) return;
        mMediaPlayer.prepare(autoStart);
    }

    public void start() {
        if (mVideoController != null && mVideoController.interceptStart()) return;
        mMediaPlayer.start();
    }

    public void restart() {
        if (mVideoController != null && mVideoController.interceptRestart()) return;
        mMediaPlayer.restart();
    }

    public void retry() {
        if (mVideoController != null && mVideoController.interceptRetry()) return;
        mMediaPlayer.retry();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public boolean isExecuteStart() {
        return mMediaPlayer.isExecuteStart();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public boolean isReleased() {
        return mMediaPlayer.isReleased();
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

    private class InnerSurfaceListener implements IVideoSurface.SurfaceListener {

        private Surface mSurface;

        @Override
        public void onSurfaceCreate(Surface surface) {
            this.mSurface = surface;
            mMediaPlayer.setSurface(mSurface);
            for (IVideoSurface.SurfaceListener listener : mSurfaceListeners) {
                listener.onSurfaceCreate(surface);
            }
        }

        @Override
        public void onSurfaceSizeChanged(Surface surface, int width, int height) {
            for (IVideoSurface.SurfaceListener listener : mSurfaceListeners) {
                listener.onSurfaceSizeChanged(surface, width, height);
            }
        }

        @Override
        public void onSurfaceDestroyed(Surface surface) {
            for (IVideoSurface.SurfaceListener listener : mSurfaceListeners) {
                listener.onSurfaceDestroyed(surface);
            }
        }
    }

    private class InnerMediaEventListener implements MediaEventListener {

        @Override
        public void onInitialized() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onInitialized();
            }
        }

        @Override
        public void onPreparing() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPreparing();
            }
        }

        @Override
        public void onPrepared(int duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPrepared(duration);
            }
        }

        @Override
        public void onStart(int currentPosition, int duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onStart(currentPosition, duration);
            }
        }

        @Override
        public void onPlaying(int currentPosition, int duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPlaying(currentPosition, duration);
            }
        }

        @Override
        public void onPause(int currentPosition, int duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPause(currentPosition, duration);
            }
        }

        @Override
        public void onStop(int currentPosition, int duration) {
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
        public void onSeekStart(int seekMilliSeconds, int seekProgress) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onSeekStart(seekMilliSeconds, seekProgress);
            }
        }

        @Override
        public void onSeekEnd() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onSeekEnd();
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
            mVideoSurface.setVideoSize(width, height);
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
        public void onRelease(int currentPosition, int duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onRelease(currentPosition, duration);
            }
        }
    }

    private class InnerMediaProgressListener implements MediaProgressListener {

        @Override
        public void onMediaProgress(int progress, int currentPosition) {
            for (MediaProgressListener listener : mMediaProgressListeners) {
                listener.onMediaProgress(progress, currentPosition);
            }
        }
    }


    public enum ScaleType {
        FIT_XY,
        FIT_CENTER,
        CENTER_INSIDE;
    }
}
