package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

public class DefaultVideoPreview extends FrameLayout implements IVideoPreview {

    private final IVideoSurface.SurfaceListener mPreviewSurfaceListener = new PreviewSurfaceListener();
    private final MediaEventListener mPreviewMediaEventListener = new PreviewMediaEventListener();

    private ISurfaceMeasurer mSurfaceMeasurer;
    private ImageView mPreviewImage;
    private int mVideoWidth;
    private int mVideoHeight;


    public DefaultVideoPreview(Context context) {
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
    public void setUp(HappyVideo happyVideo, IMediaInfo mediaInfo) {
        happyVideo.addSurfaceListener(mPreviewSurfaceListener);
        happyVideo.addMediaEventListener(mPreviewMediaEventListener);
        this.setBackgroundColor(0xFFE8E8E8);
        requestFirstFrame(mediaInfo);
        int mediaState = happyVideo.getMediaState();
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
                    DefaultVideoPreview.this.setBackgroundColor(Color.TRANSPARENT);
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

    private class PreviewMediaEventListener extends SimpleMediaEventListener {

        @Override
        public void onPlaying(long currentPosition, long duration) {
            super.onPlaying(currentPosition, duration);
            if (DefaultVideoPreview.this.getVisibility() == View.VISIBLE) {
                DefaultVideoPreview.this.setVisibility(View.GONE);
            }
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            super.onStop(currentPosition, duration);
            if (DefaultVideoPreview.this.getVisibility() == INVISIBLE || DefaultVideoPreview.this.getVisibility() == GONE) {
                DefaultVideoPreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onError(int what, int extra) {
            super.onError(what, extra);
            if (DefaultVideoPreview.this.getVisibility() == INVISIBLE || DefaultVideoPreview.this.getVisibility() == GONE) {
                DefaultVideoPreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            if (DefaultVideoPreview.this.getVisibility() == INVISIBLE || DefaultVideoPreview.this.getVisibility() == GONE) {
                DefaultVideoPreview.this.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            super.onRelease(currentPosition, duration);
            if (DefaultVideoPreview.this.getVisibility() == INVISIBLE || DefaultVideoPreview.this.getVisibility() == GONE) {
                DefaultVideoPreview.this.setVisibility(VISIBLE);
            }
        }
    }


    private class PreviewSurfaceListener implements IVideoSurface.SurfaceListener {

        @Override
        public void onSurfaceCreate(Surface surface) {
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
            if (DefaultVideoPreview.this.getVisibility() == INVISIBLE || DefaultVideoPreview.this.getVisibility() == GONE) {
                DefaultVideoPreview.this.setVisibility(VISIBLE);
            }
        }
    }
}
