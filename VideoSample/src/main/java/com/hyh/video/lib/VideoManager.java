package com.hyh.video.lib;

/**
 * Created by Eric_He on 2019/3/10.
 */

public class VideoManager {

    private final Object mLock = new Object();

    private static VideoManager sInstance = new VideoManager();

    public static VideoManager getInstance() {
        return sInstance;
    }

    private VideoDelegate mCurrentStartVideo;

    void addVideo(VideoDelegate video) {
        video.addMediaEventListener(new VideoListener(video));
    }

    public boolean hasStartVideo() {
        synchronized (mLock) {
            return mCurrentStartVideo != null && mCurrentStartVideo.isExecuteStart();
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
                if (mCurrentStartVideo == mVideo) {
                    return;
                }
                if (mCurrentStartVideo != null) {
                    mCurrentStartVideo.pause();
                }
                mCurrentStartVideo = mVideo;
            }
        }

        private void handleVideoEnd() {
            synchronized (mLock) {
                if (mCurrentStartVideo == mVideo) {
                    mCurrentStartVideo = null;
                }
            }
        }
    }
}