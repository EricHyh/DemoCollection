package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

@SuppressLint("AppCompatCustomView")
public class ImagePreview extends ImageView implements IVideoPreview {

    private final SurfaceDestroyTask mSurfaceDestroyTask = new SurfaceDestroyTask(this);
    private final MediaEventListener mPreviewMediaEventListener = new PreviewMediaEventListener();

    private VideoDelegate mVideoDelegate;
    private DataSource mDataSource;

    public ImagePreview(Context context) {
        super(context);
        setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
    }

    @Override
    public void setUp(VideoDelegate videoDelegate, IMediaInfo mediaInfo) {
        this.mVideoDelegate = videoDelegate;
        videoDelegate.addMediaEventListener(mPreviewMediaEventListener);

        DataSource dataSource = videoDelegate.getDataSource();
        if (mDataSource != null && !mDataSource.equals(dataSource)) {
            setImageBitmap(null);
        }
        mDataSource = dataSource;
        int mediaState = videoDelegate.getMediaState();
        if (mediaState == IMediaPlayer.State.IDLE
                || mediaState == IMediaPlayer.State.INITIALIZED
                || mediaState == IMediaPlayer.State.PREPARING
                || mediaState == IMediaPlayer.State.PREPARED
                || mediaState == IMediaPlayer.State.STOPPED
                || mediaState == IMediaPlayer.State.COMPLETED
                || mediaState == IMediaPlayer.State.ERROR
                || mediaState == IMediaPlayer.State.END) {
            if (this.getVisibility() == INVISIBLE || this.getVisibility() == GONE) {
                this.setVisibility(VISIBLE);
            }
        } else {
            if (this.getVisibility() == VISIBLE) {
                this.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onSurfaceCreate(Surface surface) {
        mSurfaceDestroyTask.remove();
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
        if (mVideoDelegate != null && mVideoDelegate.isPlaying()) {
            mSurfaceDestroyTask.post();
        } else {
            if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                ImagePreview.this.setVisibility(VISIBLE);
            }
        }
    }

    private class PreviewMediaEventListener extends SimpleMediaEventListener {

        @Override
        public void onPlaying(long currentPosition, long duration) {
            super.onPlaying(currentPosition, duration);
            if (ImagePreview.this.getVisibility() == View.VISIBLE) {
                ImagePreview.this.setVisibility(View.GONE);
            }
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            super.onStop(currentPosition, duration);
            if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                ImagePreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onError(int what, int extra) {
            super.onError(what, extra);
            if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                ImagePreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                ImagePreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            super.onRelease(currentPosition, duration);
            if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                ImagePreview.this.setVisibility(VISIBLE);
            }
        }
    }

    private static class SurfaceDestroyTask implements Runnable {

        private Handler mHandler = new Handler(Looper.getMainLooper());
        private WeakReference<ImagePreview> mImagePreviewRef;

        SurfaceDestroyTask(ImagePreview imagePreview) {
            mImagePreviewRef = new WeakReference<>(imagePreview);
        }

        @Override
        public void run() {
            ImagePreview imagePreview = mImagePreviewRef.get();
            if (imagePreview == null) return;
            if (imagePreview.getVisibility() == INVISIBLE || imagePreview.getVisibility() == GONE) {
                imagePreview.setVisibility(VISIBLE);
            }
        }

        void post() {
            ImagePreview imagePreview = mImagePreviewRef.get();
            if (imagePreview == null) return;
            mHandler.postDelayed(this, 300);
        }

        void remove() {
            ImagePreview imagePreview = mImagePreviewRef.get();
            if (imagePreview == null) return;
            mHandler.removeCallbacks(this);
        }
    }
}
