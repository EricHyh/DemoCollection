package com.hyh.video.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

public class TextureVideo extends TextureView implements IVideoSurface {

    private final SurfaceTextureListener mTextureListener;

    private ISurfaceMeasurer mSurfaceMeasurer;

    private SurfaceListener mSurfaceListener;

    private SurfaceTexture mSurfaceTexture;

    private Surface mSurface;

    private int mVideoWidth;

    private int mVideoHeight;

    public TextureVideo(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTextureListener = new SurfaceListenerImplV16();
        } else {
            mTextureListener = new SurfaceListenerImplBase();
        }
        setSurfaceTextureListener(mTextureListener);
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
    public void reset() {
        if (mSurface != null) {
            try {
                mSurface.release();
            } catch (Throwable e) {
                //
            }
            mSurface = null;
            mSurfaceTexture = null;
        }
    }

    @Override
    public boolean isSupportRotate() {
        return true;
    }

    @Override
    public void onVideoSceneChanged(FrameLayout videoContainer, int scene) {
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


    private class SurfaceListenerImplBase implements SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mSurfaceTexture != surface) {
                mSurfaceTexture = surface;
                mSurface = new Surface(surface);
            }
            mSurfaceListener.onSurfaceCreate(mSurface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceListener.onSurfaceSizeChanged(mSurface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceListener.onSurfaceDestroyed(mSurface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private class SurfaceListenerImplV16 implements SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mSurfaceTexture == null) {
                mSurfaceTexture = surface;
                mSurface = new Surface(surface);
            } else {
                setSurfaceTexture(mSurfaceTexture);
            }
            mSurfaceListener.onSurfaceCreate(mSurface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceListener.onSurfaceSizeChanged(mSurface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceListener.onSurfaceDestroyed(mSurface);
            return mSurface == null;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }
}
