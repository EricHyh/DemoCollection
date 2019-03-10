package com.hyh.video.lib;

import android.content.Context;
import android.graphics.SurfaceTexture;
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
            ViewGroup viewGroup = (ViewGroup) parent;
            int[] size = mSurfaceMeasurer.onMeasure(viewGroup.getMeasuredWidth(),
                    viewGroup.getMeasuredHeight());
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

    private class SurfaceListenerWrapper implements SurfaceTextureListener {

        private SurfaceTexture mSurfaceTexture;

        private Surface mSurface;

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
