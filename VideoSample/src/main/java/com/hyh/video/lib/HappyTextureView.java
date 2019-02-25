package com.hyh.video.lib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */

public class HappyTextureView extends TextureView implements IVideoSurface {

    private final ISurfaceMeasurer mSurfaceMeasurer = SurfaceMeasurerFactory.create(this);

    public HappyTextureView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int[] size = mSurfaceMeasurer.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(size[0], size[1]);
    }

    @Override
    public void setSurfaceListener(SurfaceListener listener) {
        if (listener == null) {
            setSurfaceTextureListener(null);
        } else {
            setSurfaceTextureListener(new SurfaceListenerWrapper(listener));
        }
    }

    @Override
    public void setScaleType(HappyVideo.ScaleType scaleType) {
        mSurfaceMeasurer.setScaleType(scaleType);
    }

    private static class SurfaceListenerWrapper implements SurfaceTextureListener {

        private final SurfaceListener mSurfaceListener;

        private SurfaceTexture mSurfaceTexture;

        private Surface mSurface;

        SurfaceListenerWrapper(SurfaceListener listener) {
            this.mSurfaceListener = listener;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            this.mSurfaceTexture = surface;
            this.mSurface = new Surface(surface);
            mSurfaceListener.onSurfaceCreate(mSurface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            if (mSurfaceTexture != surface) {
                this.mSurfaceTexture = surface;
                this.mSurface = new Surface(surface);
            }
            mSurfaceListener.onSurfaceSizeChanged(mSurface, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mSurfaceTexture != surface) {
                this.mSurfaceTexture = surface;
                this.mSurface = new Surface(surface);
            }
            mSurfaceListener.onSurfaceDestroyed(mSurface);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

}
