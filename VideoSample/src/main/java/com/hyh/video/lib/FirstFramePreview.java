package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class FirstFramePreview extends FrameLayout implements IVideoPreview {

    private final SurfaceDestroyTask mSurfaceDestroyTask = new SurfaceDestroyTask(this);
    private final MediaEventListener mPreviewMediaEventListener = new PreviewMediaEventListener();

    private VideoDelegate mVideoDelegate;
    private ISurfaceMeasurer mSurfaceMeasurer;
    private ImageView mPreviewImage;
    private DataSource mDataSource;
    private int mVideoWidth;
    private int mVideoHeight;

    public FirstFramePreview(Context context) {
        super(context);
        {
            mPreviewImage = new ImageView(context);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(0, 0);
            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageParams.gravity = Gravity.CENTER;
            addView(mPreviewImage, imageParams);
        }
    }

    public FirstFramePreview(Context context, int backgroundColor) {
        super(context);
        {
            mPreviewImage = new ImageView(context);
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(0, 0);
            mPreviewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageParams.gravity = Gravity.CENTER;
            addView(mPreviewImage, imageParams);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
        this.mSurfaceMeasurer = surfaceMeasurer;
    }

    @Override
    public void setUp(VideoDelegate videoDelegate, IMediaInfo mediaInfo) {
        this.mVideoDelegate = videoDelegate;
        videoDelegate.addMediaEventListener(mPreviewMediaEventListener);

        DataSource dataSource = videoDelegate.getDataSource();
        if (mDataSource != null && !mDataSource.equals(dataSource)) {
            mPreviewImage.setImageBitmap(null);
        }
        mDataSource = dataSource;

        this.setBackgroundColor(0xFFE8E8E8);
        requestFirstFrame(mediaInfo);
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

    private void requestFirstFrame(IMediaInfo mediaInfo) {
        mediaInfo.getFrameAtTime(0, new IMediaInfo.Result<Bitmap>() {
            @Override
            public void onResult(Bitmap bitmap) {
                if (bitmap != null) {
                    mVideoWidth = bitmap.getWidth();
                    mVideoHeight = bitmap.getHeight();
                    mPreviewImage.setImageBitmap(bitmap);
                    resetImageSize();
                    FirstFramePreview.this.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetImageSize();
    }

    private void resetImageSize() {
        if (mSurfaceMeasurer != null) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            if (width != 0 && height != 0) {
                ViewGroup.LayoutParams layoutParams = mPreviewImage.getLayoutParams();
                mSurfaceMeasurer.setVideoWidth(mVideoWidth, mVideoHeight);
                int[] size = mSurfaceMeasurer.onMeasure(width, height);
                layoutParams.width = size[0];
                layoutParams.height = size[1];
                mPreviewImage.requestLayout();
            }
        }
    }

    @Override
    public void onSurfaceCreate(Surface surface) {
        mSurfaceDestroyTask.remove();
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        ViewGroup.LayoutParams layoutParams = mPreviewImage.getLayoutParams();
        boolean isSizeChanged = false;
        if (layoutParams.width != width) {
            layoutParams.width = width;
            isSizeChanged = true;
        }
        if (layoutParams.height != height) {
            layoutParams.height = height;
            isSizeChanged = true;
        }
        if (isSizeChanged) {
            mPreviewImage.requestLayout();
        }
    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
        /*if (mVideoDelegate != null && (mVideoDelegate.isStartFullscreenSceneJustNow() || mVideoDelegate.isRecoverNormalSceneJustNow())) {
                return;
            }
            if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                FirstFramePreview.this.setVisibility(VISIBLE);
            }*/
        if (mVideoDelegate != null && mVideoDelegate.isExecuteStart()) {
            mSurfaceDestroyTask.post();
        } else {
            if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                FirstFramePreview.this.setVisibility(VISIBLE);
                Log.d("MediaSystem", "onSurfaceDestroyed: FirstFramePreview: ");
            }
        }
    }

    private class PreviewMediaEventListener extends SimpleMediaEventListener {

        @Override
        public void onPlaying(long currentPosition, long duration) {
            super.onPlaying(currentPosition, duration);
            if (FirstFramePreview.this.getVisibility() == View.VISIBLE) {
                FirstFramePreview.this.setVisibility(View.GONE);
            }
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            super.onStop(currentPosition, duration);
            if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                FirstFramePreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onError(int what, int extra) {
            super.onError(what, extra);
            if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                FirstFramePreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                FirstFramePreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            super.onRelease(currentPosition, duration);
            if (FirstFramePreview.this.getVisibility() == INVISIBLE || FirstFramePreview.this.getVisibility() == GONE) {
                FirstFramePreview.this.setVisibility(VISIBLE);
            }
        }
    }

    private static class SurfaceDestroyTask implements Runnable {

        private Handler mHandler = new Handler(Looper.getMainLooper());
        private WeakReference<FirstFramePreview> mFirstFramePreviewRef;

        SurfaceDestroyTask(FirstFramePreview firstFramePreview) {
            mFirstFramePreviewRef = new WeakReference<>(firstFramePreview);
        }

        @Override
        public void run() {
            FirstFramePreview firstFramePreview = mFirstFramePreviewRef.get();
            if (firstFramePreview == null) return;
            if (firstFramePreview.getVisibility() == INVISIBLE || firstFramePreview.getVisibility() == GONE) {
                firstFramePreview.setVisibility(VISIBLE);
            }
        }

        void post() {
            FirstFramePreview firstFramePreview = mFirstFramePreviewRef.get();
            if (firstFramePreview == null) return;
            mHandler.postDelayed(this, 300);
        }

        void remove() {
            FirstFramePreview firstFramePreview = mFirstFramePreviewRef.get();
            if (firstFramePreview == null) return;
            mHandler.removeCallbacks(this);
        }
    }
}