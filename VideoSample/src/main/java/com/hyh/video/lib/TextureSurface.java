package com.hyh.video.lib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

public class TextureSurface extends TextureView implements IVideoSurface {

    private ISurfaceMeasurer mSurfaceMeasurer;

    private SurfaceListener mSurfaceListener;

    private int mVideoWidth;

    private int mVideoHeight;

    public TextureSurface(Context context) {
        super(context);
        setSurfaceTextureListener(new SurfaceListenerWrapper());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewParent parent = getParent();
        if (mSurfaceMeasurer == null || parent == null || !(parent instanceof ViewGroup)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int defaultWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int defaultHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int[] size = mSurfaceMeasurer.onMeasure(defaultWidth, defaultHeight, mVideoWidth, mVideoHeight);
            setMeasuredDimension(size[0], size[1]);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setSurfaceListener(SurfaceListener listener) {
        this.mSurfaceListener = listener;
    }

    @Override
    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
        this.mSurfaceMeasurer = surfaceMeasurer;
        requestLayout();
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        boolean isSizeChanged = false;
        if (mVideoWidth != videoWidth) {
            this.mVideoWidth = videoWidth;
            isSizeChanged = true;
        }
        if (mVideoHeight != videoHeight) {
            this.mVideoHeight = videoHeight;
            isSizeChanged = true;
        }
        if (isSizeChanged) {
            requestLayout();
        }
    }

    private class SurfaceListenerWrapper implements SurfaceTextureListener {

        private SurfaceTexture mSurfaceTexture;

        private Surface mSurface;

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mSurfaceTexture == null) {
                this.mSurfaceTexture = surface;
                this.mSurface = new Surface(surface);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    setSurfaceTexture(mSurfaceTexture);
                }
            }
            mSurfaceListener.onSurfaceCreate(mSurface);

            /*if (mSurfaceTexture != surface) {
                this.mSurfaceTexture = surface;
                this.mSurface = new Surface(surface);
            }
            mSurfaceListener.onSurfaceCreate(mSurface);*/
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceListener.onSurfaceSizeChanged(mSurface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceListener.onSurfaceDestroyed(mSurface);
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }
}
