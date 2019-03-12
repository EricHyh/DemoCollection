package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

/**
 * @author Administrator
 * @description
 * @data 2019/2/25
 */

@SuppressLint("AppCompatCustomView")
public class ImagePreview extends ImageView implements IVideoPreview {

    private final IVideoSurface.SurfaceListener mPreviewSurfaceListener = new PreviewSurfaceListener();
    private final MediaEventListener mPreviewMediaEventListener = new PreviewMediaEventListener();

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
    public void setUp(HappyVideo happyVideo, IMediaInfo mediaInfo) {
        happyVideo.addSurfaceListener(mPreviewSurfaceListener);
        happyVideo.addMediaEventListener(mPreviewMediaEventListener);

        DataSource dataSource = happyVideo.getDataSource();
        if (mDataSource != null && !mDataSource.equals(dataSource)) {
            setImageBitmap(null);
        }
        mDataSource = dataSource;
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


    private class PreviewSurfaceListener implements IVideoSurface.SurfaceListener {

        @Override
        public void onSurfaceCreate(Surface surface) {
        }

        @Override
        public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        }

        @Override
        public void onSurfaceDestroyed(Surface surface) {
            if (ImagePreview.this.getVisibility() == INVISIBLE || ImagePreview.this.getVisibility() == GONE) {
                ImagePreview.this.setVisibility(VISIBLE);
            }
        }
    }
}
