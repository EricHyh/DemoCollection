package com.hyh.video.lib;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

/**
 * Created by Eric_He on 2019/3/22.
 */

public class VideoPreviewHelper {

    private final PreviewAction mPreviewAction;
    private final SurfaceDestroyTask mSurfaceDestroyTask = new SurfaceDestroyTask(this);
    private final MediaEventListener mPreviewMediaEventListener = new PreviewMediaEventListener();

    private VideoDelegate mVideoDelegate;

    public VideoPreviewHelper(PreviewAction previewAction) {
        this.mPreviewAction = previewAction;
    }

    public void setUp(VideoDelegate videoDelegate) {
        this.mVideoDelegate = videoDelegate;
        videoDelegate.addMediaEventListener(mPreviewMediaEventListener);
        int mediaState = videoDelegate.getMediaState();
        if (mediaState == IMediaPlayer.State.IDLE
                || mediaState == IMediaPlayer.State.INITIALIZED
                || mediaState == IMediaPlayer.State.PREPARING
                || mediaState == IMediaPlayer.State.PREPARED
                || mediaState == IMediaPlayer.State.STOPPED
                || mediaState == IMediaPlayer.State.COMPLETED
                || mediaState == IMediaPlayer.State.ERROR
                || mediaState == IMediaPlayer.State.END) {
            mPreviewAction.showPreview();
        } else {
            mPreviewAction.hidePreview();
        }
    }

    public void onSurfaceCreate() {
        mSurfaceDestroyTask.remove();
    }

    public void onSurfaceDestroyed() {
        if (mVideoDelegate != null && mVideoDelegate.isPlaying()) {
            mSurfaceDestroyTask.post();
        } else {
            mPreviewAction.showPreview();
        }
    }

    private class PreviewMediaEventListener extends SimpleMediaEventListener {

        private boolean mIsReceiveOnPlaying;

        @Override
        public void onInitialized() {
            super.onInitialized();
            mIsReceiveOnPlaying = false;
        }

        @Override
        public void onStart(long currentPosition, long duration, int bufferingPercent) {
            super.onStart(currentPosition, duration, bufferingPercent);
            if (mIsReceiveOnPlaying) {
                if (bufferingPercent > 0 && mVideoDelegate.isPlaying()) {
                    int progress = 0;
                    if (duration > 0) {
                        progress = Math.round((currentPosition * 1.0f / duration) * 100);
                    }
                    if (bufferingPercent >= progress) {
                        mPreviewAction.hidePreview();
                    }
                }
            }
        }

        @Override
        public void onPlaying(long currentPosition, long duration) {
            super.onPlaying(currentPosition, duration);
            mIsReceiveOnPlaying = true;
            mPreviewAction.hidePreview();
        }


        @Override
        public void onStop(long currentPosition, long duration) {
            super.onStop(currentPosition, duration);
            mPreviewAction.showPreview();
        }

        @Override
        public void onBufferingUpdate(int percent) {
            super.onBufferingUpdate(percent);
            if (mIsReceiveOnPlaying && percent > 0) {
                mPreviewAction.hidePreview();
            }
        }

        @Override
        public void onError(int what, int extra) {
            super.onError(what, extra);
            mIsReceiveOnPlaying = false;
            mPreviewAction.showPreview();
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            mPreviewAction.showPreview();
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            super.onRelease(currentPosition, duration);
            mIsReceiveOnPlaying = false;
            mPreviewAction.showPreview();
        }
    }

    private static class SurfaceDestroyTask implements Runnable {

        private Handler mHandler = new Handler(Looper.getMainLooper());
        private WeakReference<VideoPreviewHelper> mPreviewHelperRef;
        private volatile boolean mIsRemove;

        SurfaceDestroyTask(VideoPreviewHelper previewHelper) {
            mPreviewHelperRef = new WeakReference<>(previewHelper);
        }

        @Override
        public void run() {
            if (mIsRemove) return;
            VideoPreviewHelper previewHelper = mPreviewHelperRef.get();
            if (previewHelper == null) return;
            previewHelper.mPreviewAction.showPreview();
        }

        void post() {
            mIsRemove = false;
            VideoPreviewHelper previewHelper = mPreviewHelperRef.get();
            if (previewHelper == null) return;
            mHandler.postDelayed(this, 300);
        }

        void remove() {
            mIsRemove = true;
            VideoPreviewHelper previewHelper = mPreviewHelperRef.get();
            if (previewHelper == null) return;
            mHandler.removeCallbacks(this);
        }
    }

    public interface PreviewAction {

        void showPreview();

        void hidePreview();

    }
}