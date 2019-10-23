package com.hyh.video.lib;

import java.lang.ref.WeakReference;

/**
 * Created by Eric_He on 2019/3/10.
 */

public class VideoManager {

    private final Object mLock = new Object();

    private static VideoManager sInstance = new VideoManager();

    public static VideoManager getInstance() {
        return sInstance;
    }

    private WeakReference<VideoDelegate> mCurrentStartVideoRef;

    void addVideo(VideoDelegate video) {
        video.addMediaEventListener(new VideoListener(video));
    }

    public boolean hasStartVideo() {
        synchronized (mLock) {
            final WeakReference<VideoDelegate> currentStartVideoRef = mCurrentStartVideoRef;
            if (currentStartVideoRef != null) {
                final VideoDelegate videoDelegate = currentStartVideoRef.get();
                return videoDelegate != null && videoDelegate.isExecuteStart();
            }
            return false;
        }
    }

    public void pauseCurrent() {
        synchronized (mLock) {
            final WeakReference<VideoDelegate> currentStartVideoRef = mCurrentStartVideoRef;
            if (currentStartVideoRef != null) {
                final VideoDelegate currentStartVideo = currentStartVideoRef.get();
                if (currentStartVideo != null) {
                    currentStartVideo.pause();
                }
            }
        }
    }

    public void stopCurrent() {
        synchronized (mLock) {
            final WeakReference<VideoDelegate> currentStartVideoRef = mCurrentStartVideoRef;
            if (currentStartVideoRef != null) {
                VideoDelegate currentStartVideo = currentStartVideoRef.get();
                if (currentStartVideo != null) {
                    currentStartVideo.stop();
                }
            }
        }
    }

    public void releaseCurrent() {
        synchronized (mLock) {
            final WeakReference<VideoDelegate> currentStartVideoRef = mCurrentStartVideoRef;
            if (currentStartVideoRef != null) {
                VideoDelegate currentStartVideo = currentStartVideoRef.get();
                if (currentStartVideo != null) {
                    currentStartVideo.release();
                }
            }
        }
    }

    private class VideoListener extends SimpleMediaEventListener {

        private VideoDelegate mVideo;

        VideoListener(VideoDelegate video) {
            mVideo = video;
        }

        @Override
        public void onPreparing(boolean autoStart) {
            super.onPreparing(autoStart);
            if (autoStart) {
                handleVideoStart();
            }
        }

        @Override
        public void onExecuteStart() {
            super.onExecuteStart();
            handleVideoStart();
        }

        @Override
        public void onPause(long currentPosition, long duration) {
            super.onPause(currentPosition, duration);
            handleVideoEnd();
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            super.onStop(currentPosition, duration);
            handleVideoEnd();
        }

        @Override
        public void onError(int what, int extra) {
            super.onError(what, extra);
            handleVideoEnd();
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            handleVideoEnd();
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            super.onRelease(currentPosition, duration);
            handleVideoEnd();
        }

        private void handleVideoStart() {
            synchronized (mLock) {
                final WeakReference<VideoDelegate> currentStartVideoRef = mCurrentStartVideoRef;
                VideoDelegate currentStartVideo = currentStartVideoRef == null ? null : currentStartVideoRef.get();
                if (currentStartVideo == mVideo) {
                    return;
                }
                if (currentStartVideo != null) {
                    currentStartVideo.pause();
                }
                mCurrentStartVideoRef = new WeakReference<>(mVideo);
            }
        }

        private void handleVideoEnd() {
            synchronized (mLock) {
                final WeakReference<VideoDelegate> currentStartVideoRef = mCurrentStartVideoRef;
                VideoDelegate currentStartVideo = currentStartVideoRef == null ? null : currentStartVideoRef.get();
                if (currentStartVideo == mVideo) {
                    mCurrentStartVideoRef = null;
                }
            }
        }
    }
}