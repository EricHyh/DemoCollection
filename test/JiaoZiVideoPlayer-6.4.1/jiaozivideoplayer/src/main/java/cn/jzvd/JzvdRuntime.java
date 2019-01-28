package com.yly.mob.ssp.video;

import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;

/**
 * @author Administrator
 * @description
 * @data 2019/1/27
 */

public class JzvdRuntime implements TextureView.SurfaceTextureListener {


    private static final int MESSAGE_PREPARE = 1;
    private static final int MESSAGE_RELEASE = 2;

    public final JZMediaInterface mJzMediaInterface;
    public final JZTextureView mTextureView;
    public SurfaceTexture mSavedSurfaceTexture;
    public Surface mSurface;


    JzvdRuntime(JZMediaInterface jzMediaInterface, JZTextureView textureView) {
        this.mJzMediaInterface = jzMediaInterface;
        this.mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);
    }

    void prepare() {
        releaseMediaPlayer();
        mMainThreadHandler.sendEmptyMessage(MESSAGE_PREPARE);
    }

    /*void start() {

    }

    void pause() {

    }*/

    void releaseMediaPlayer() {
        mMainThreadHandler.removeCallbacksAndMessages(null);
        mMainThreadHandler.sendEmptyMessage(MESSAGE_RELEASE);
    }

    public void release() {
        mJzMediaInterface.release();
        if (mTextureView != null && mTextureView.getParent() != null) {
            ((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mSavedSurfaceTexture != null) {
            mSavedSurfaceTexture.release();
            mSavedSurfaceTexture = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSavedSurfaceTexture == null) {
            mSavedSurfaceTexture = surface;
            prepare();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mTextureView.setSurfaceTexture(mSavedSurfaceTexture);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mSavedSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_PREPARE: {
                    JZMediaManager.instance().setVideoWidth(0);
                    JZMediaManager.instance().setVideoHeight(0);

                    mJzMediaInterface.prepare();
                    if (mSavedSurfaceTexture != null) {
                        if (mSurface != null) {
                            mSurface.release();
                        }
                        mSurface = new Surface(mSavedSurfaceTexture);
                        mJzMediaInterface.setSurface(mSurface);
                    }
                    break;
                }
                case MESSAGE_RELEASE: {
                    mJzMediaInterface.release();
                    break;
                }
            }
        }
    };

    boolean isFullScreenShow() {
        return false;
    }

    boolean closeFullScreen() {
        return false;
    }
}
