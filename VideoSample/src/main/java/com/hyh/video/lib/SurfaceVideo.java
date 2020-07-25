package com.hyh.video.lib;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by Eric_He on 2019/5/19.
 */

public class SurfaceVideo extends SurfaceView implements IVideoSurface {

    private static final String TAG = "SurfaceVideoView";

    private final SurfaceHolderCallback mSurfaceHolderCallback;

    private ISurfaceMeasurer mSurfaceMeasurer;

    private SurfaceListener mSurfaceListener;

    private Surface mSurface;

    private int mVideoWidth;

    private int mVideoHeight;


    public SurfaceVideo(Context context) {
        super(context);
        mSurfaceHolderCallback = new SurfaceHolderCallback();
        getHolder().addCallback(mSurfaceHolderCallback);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewParent parent = getParent();
        if (mSurfaceMeasurer == null || !(parent instanceof ViewGroup)) {
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

    @Override
    public void setSurfaceListener(SurfaceListener listener) {
        this.mSurfaceListener = listener;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean isSupportRotate() {
        return false;
    }

    @Override
    public void onVideoSceneChanged(FrameLayout videoContainer, int scene) {
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback2 {

        @Override
        public void surfaceRedrawNeeded(SurfaceHolder holder) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurface = holder.getSurface();
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceCreate(holder.getSurface());
            }
            Log.d(TAG, "surfaceCreated: holder = " + holder.getSurface());
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceSizeChanged(holder.getSurface(), width, height);
            }
            Log.d(TAG, "surfaceChanged: holder = " + holder.getSurface());
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mSurfaceListener != null) {
                mSurfaceListener.onSurfaceDestroyed(holder.getSurface());
            }
            Log.d(TAG, "surfaceDestroyed: holder = " + holder.getSurface());
        }
    }
}
