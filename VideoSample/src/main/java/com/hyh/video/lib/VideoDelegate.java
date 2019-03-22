package com.hyh.video.lib;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/3/18
 */
@SuppressWarnings("all")
public class VideoDelegate {

    private static final String TAG = "VideoDelegate";
    private static final VideoManager VIDEO_MANAGER = new VideoManager();

    protected final IMediaPlayer mMediaPlayer = new MediaSystem();
    protected final Context mContext;
    protected final IMediaInfo mMediaInfo;
    protected final IVideoSurface.SurfaceListener mSurfaceListener = new InnerSurfaceListener();
    protected final MediaEventListener mMediaEventListener = new InnerMediaEventListener();
    protected final MediaProgressListener mMediaProgressListener = new InnerMediaProgressListener();

    protected final List<MediaEventListener> mMediaEventListeners = new ArrayList<>();
    protected final List<MediaProgressListener> mMediaProgressListeners = new ArrayList<>();
    protected final List<IVideoSurface.SurfaceListener> mSurfaceListeners = new ArrayList<>();

    protected final WindowAttachListenerView mWindowAttachListenerView;

    protected ISurfaceMeasurer mSurfaceMeasurer = new FitCenterMeasurer();
    protected IVideoBackground mVideoBackground;
    protected IVideoSurface mVideoSurface;
    protected IVideoPreview mVideoPreview;
    protected IVideoController mVideoController;

    protected FrameLayout mNormalVideoContainer;
    protected FrameLayout mVideoContainer;

    protected int mScene = Scene.NORMAL;
    protected long mStartFullscreenSceneTimeMills;
    protected long mRecoverNormalSceneTimeMills;

    protected CharSequence mTitle;

    public VideoDelegate(Context context) {
        this.mContext = context;
        mWindowAttachListenerView = new WindowAttachListenerView(context);
        mMediaPlayer.setMediaEventListener(mMediaEventListener);
        mMediaPlayer.setMediaProgressListener(mMediaProgressListener);
        mMediaInfo = new MediaInfoImpl(context);

        this.mVideoBackground = newVideoBackground(context);
        this.mVideoSurface = newVideoSurface(context);
        this.mVideoPreview = newVideoPreview(context);
        this.mVideoController = newVideoController(context);

        if (mVideoSurface != null) {
            mVideoSurface.setSurfaceMeasurer(mSurfaceMeasurer);
            mVideoSurface.setSurfaceListener(mSurfaceListener);
        }
        if (mVideoPreview != null) {
            mVideoPreview.setSurfaceMeasurer(mSurfaceMeasurer);
        }
    }

    public void attachedToContainer(FrameLayout videoContainer) {
        mVideoContainer = videoContainer;

        if (mVideoBackground != null) {
            mVideoContainer.setBackgroundDrawable(mVideoBackground.getBackgroundDrawable());
            if (mVideoBackground.getBackgroundView() != null) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mVideoContainer.addView(mVideoBackground.getBackgroundView(), params);
            }
        }
        if (mVideoSurface != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            mVideoContainer.addView(mVideoSurface.getView(), params);
        }
        if (mVideoPreview != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoContainer.addView(mVideoPreview.getView(), params);
        }
        if (mVideoController != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoContainer.addView(mVideoController.getView(), params);
        }
    }

    public void detachedFromContainer() {
        if (mVideoContainer != null) {
            if (mVideoBackground != null && mVideoBackground.getBackgroundView() != null) {
                mVideoContainer.removeView(mVideoBackground.getBackgroundView());
            }
            if (mVideoSurface != null) {
                mVideoContainer.removeView(mVideoSurface.getView());
            }
            if (mVideoPreview != null) {
                mVideoContainer.removeView(mVideoPreview.getView());
            }
            if (mVideoController != null) {
                mVideoContainer.removeView(mVideoController.getView());
            }
        }
    }

    protected IVideoBackground newVideoBackground(Context context) {
        return new DefaultVideoBackground();
    }

    protected IVideoSurface newVideoSurface(Context context) {
        return new TextureSurface(context);
    }

    protected IVideoPreview newVideoPreview(Context context) {
        return new FirstFramePreview(context);
    }

    protected IVideoController newVideoController(Context context) {
        return new DefaultVideoController(context);
    }

    protected void onAttachedToWindow(View view) {
        ViewParent parent = mWindowAttachListenerView.getParent();
        if (parent == null) {
            ((ViewGroup) view.getRootView()).addView(mWindowAttachListenerView);
        }
    }

    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
        if (surfaceMeasurer == null) throw new NullPointerException("HappyVideo setSurfaceMeasurer can't be null");
        this.mSurfaceMeasurer = surfaceMeasurer;
        if (mVideoSurface != null) {
            mVideoSurface.setSurfaceMeasurer(mSurfaceMeasurer);
        }
        if (mVideoPreview != null) {
            mVideoPreview.setSurfaceMeasurer(mSurfaceMeasurer);
        }
        if (mVideoContainer != null) {
            mVideoContainer.requestLayout();
        }
    }

    public void setVideoBackground(IVideoBackground background) {
        if (mVideoBackground == background) return;
        if (mVideoBackground != null) {
            mVideoContainer.setBackgroundDrawable(null);
            if (mVideoBackground.getBackgroundView() != null) {
                mVideoContainer.removeView(mVideoBackground.getBackgroundView());
            }
        }
        this.mVideoBackground = background;
        if (mVideoBackground != null) {
            mVideoContainer.setBackgroundDrawable(mVideoBackground.getBackgroundDrawable());
            if (mVideoBackground != null) {
                mVideoContainer.addView(mVideoBackground.getBackgroundView(), 0);
            }
        }
    }

    public void setVideoPreview(IVideoPreview videoPreview) {
        if (mVideoPreview == videoPreview) return;
        if (mVideoPreview != null) {
            mVideoContainer.removeView(mVideoPreview.getView());
        }
        this.mVideoPreview = videoPreview;
        if (mVideoPreview != null) {
            mVideoPreview.setSurfaceMeasurer(mSurfaceMeasurer);
            if (mVideoBackground != null && mVideoBackground.getBackgroundView() != null) {
                mVideoContainer.addView(mVideoPreview.getView(), 2);
            } else {
                mVideoContainer.addView(mVideoPreview.getView(), 1);
            }
            if (getDataSource() != null) {
                mVideoPreview.setUp(this, mMediaInfo);
            }
        }
    }

    public void setVideoController(IVideoController controller) {
        if (mVideoController == controller) return;
        if (mVideoController != null) {
            mVideoContainer.removeView(mVideoController.getView());
        }
        this.mVideoController = controller;
        if (mVideoController != null) {
            mVideoContainer.addView(mVideoController.getView());
            if (getDataSource() != null) {
                mVideoController.setup(this, mTitle, mMediaInfo);
            }
        }
    }

    public void addMediaEventListener(MediaEventListener listener) {
        if (listener == null || mMediaEventListeners.contains(listener)) return;
        mMediaEventListeners.add(listener);
    }

    public void removeMediaEventListener(MediaEventListener listener) {
        mMediaEventListeners.remove(listener);
    }

    public void addMediaProgressListener(MediaProgressListener listener) {
        if (listener == null || mMediaProgressListeners.contains(listener)) return;
        mMediaProgressListeners.add(listener);
    }

    public void removeMediaProgressListener(MediaProgressListener listener) {
        mMediaProgressListeners.remove(listener);
    }

    public void addSurfaceListener(IVideoSurface.SurfaceListener listener) {
        if (listener == null || mSurfaceListeners.contains(listener)) return;
        mSurfaceListeners.add(listener);
    }

    public void removeSurfaceListener(IVideoSurface.SurfaceListener listener) {
        mSurfaceListeners.remove(listener);
    }

    public boolean setup(DataSource source, CharSequence title, boolean looping) {
        boolean set = mMediaPlayer.setDataSource(source);
        if (set) {
            mMediaInfo.setup(source);
            this.mTitle = title;
            mMediaPlayer.setLooping(looping);
            if (mVideoPreview != null) {
                mVideoPreview.setUp(this, mMediaInfo);
            }
            if (mVideoController != null) {
                mVideoController.setup(this, title, mMediaInfo);
            }
            mVideoSurface.reset();
        }
        return set;
    }

    public int getScene() {
        return mScene;
    }

    public boolean startFullscreenScene() {
        if (mVideoContainer.getWindowToken() == null) return false;
        mScene = Scene.FULLSCREEN;
        mStartFullscreenSceneTimeMills = System.currentTimeMillis();
        ViewGroup rootView = (ViewGroup) mVideoContainer.getRootView();
        mNormalVideoContainer = mVideoContainer;
        detachedFromContainer();
        HappyVideo happyVideo = new HappyVideo(mContext, null, 0, this);
        /*happyVideo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                *//*| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN*//*);*/
        //((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
        //((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
        rootView.addView(happyVideo, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return true;
    }

    public boolean isStartFullscreenSceneJustNow() {
        if (mScene == Scene.FULLSCREEN) {
            long currentTimeMillis = System.currentTimeMillis();
            Log.d("TTT", "startFullscreenScene: isStartFullscreenSceneJustNow currentTimeMillis = " + currentTimeMillis);
            long timeInterval = Math.abs(currentTimeMillis - mStartFullscreenSceneTimeMills);
            if (timeInterval < 300) {
                return true;
            }
        }
        return false;
    }

    public boolean recoverNormalScene() {
        if (mScene != Scene.FULLSCREEN) return false;
        mScene = Scene.NORMAL;
        mRecoverNormalSceneTimeMills = System.currentTimeMillis();
        ViewGroup parent = (ViewGroup) mVideoContainer.getParent();
        if (parent != null) {
            parent.removeView(mVideoContainer);
        }
        detachedFromContainer();
        attachedToContainer(mNormalVideoContainer);
        mNormalVideoContainer = null;
        return true;
    }

    public boolean isRecoverNormalSceneJustNow() {
        if (mScene == Scene.NORMAL) {
            long currentTimeMillis = System.currentTimeMillis();
            long timeInterval = Math.abs(currentTimeMillis - mRecoverNormalSceneTimeMills);
            if (timeInterval < 300) {
                return true;
            }
        }
        return false;
    }

    public int getMediaState() {
        return mMediaPlayer.getMediaState();
    }

    public DataSource getDataSource() {
        return mMediaPlayer.getDataSource();
    }

    public void setLooping(boolean looping) {
        mMediaPlayer.setLooping(looping);
    }

    public boolean isLooping() {
        return mMediaPlayer.isLooping();
    }

    public void prepare(boolean autoStart) {
        if (mVideoController != null && mVideoController.interceptPrepare(autoStart)) return;
        mMediaPlayer.prepare(autoStart);
        if (autoStart) {
            VIDEO_MANAGER.onStart(this);
        }
    }

    public void start() {
        if (mVideoController != null && mVideoController.interceptStart()) return;
        mMediaPlayer.start();
        VIDEO_MANAGER.onStart(this);
    }

    public void restart() {
        if (mVideoController != null && mVideoController.interceptRestart()) return;
        mMediaPlayer.restart();
        VIDEO_MANAGER.onStart(this);
    }

    public void retry() {
        if (mVideoController != null && mVideoController.interceptRetry()) return;
        mMediaPlayer.retry();
        VIDEO_MANAGER.onStart(this);
    }

    public void pause() {
        mMediaPlayer.pause();
        VIDEO_MANAGER.onEnd(this);
    }

    public void stop() {
        mMediaPlayer.stop();
        VIDEO_MANAGER.onEnd(this);
    }

    public void release() {
        mMediaPlayer.release();
        VIDEO_MANAGER.onEnd(this);
    }

    public boolean isExecuteStart() {
        return mMediaPlayer.isExecuteStart();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public boolean isReleased() {
        return mMediaPlayer.isReleased();
    }

    public void seekTimeTo(int milliSeconds) {
        mMediaPlayer.seekTimeTo(milliSeconds);
    }

    public void seekProgressTo(int progress) {
        mMediaPlayer.seekProgressTo(progress);
    }

    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public boolean isSupportSpeed() {
        return mMediaPlayer.isSupportSpeed();
    }

    public void setSpeed(float speed) {
        mMediaPlayer.setSpeed(speed);
    }

    private class InnerSurfaceListener implements IVideoSurface.SurfaceListener {

        private Surface mSurface;

        @Override
        public void onSurfaceCreate(Surface surface) {
            this.mSurface = surface;
            mMediaPlayer.setSurface(mSurface);
            for (IVideoSurface.SurfaceListener listener : mSurfaceListeners) {
                listener.onSurfaceCreate(surface);
            }
            if (mVideoPreview != null) {
                mVideoPreview.onSurfaceCreate(surface);
            }
            if (mVideoController != null) {
                mVideoController.onSurfaceCreate(surface);
            }
        }

        @Override
        public void onSurfaceSizeChanged(Surface surface, int width, int height) {
            for (IVideoSurface.SurfaceListener listener : mSurfaceListeners) {
                listener.onSurfaceSizeChanged(surface, width, height);
            }
            if (mVideoPreview != null) {
                mVideoPreview.onSurfaceSizeChanged(surface, width, height);
            }
            if (mVideoController != null) {
                mVideoController.onSurfaceSizeChanged(surface, width, height);
            }
        }

        @Override
        public void onSurfaceDestroyed(Surface surface) {
            for (IVideoSurface.SurfaceListener listener : mSurfaceListeners) {
                listener.onSurfaceDestroyed(surface);
            }
            if (mVideoPreview != null) {
                mVideoPreview.onSurfaceDestroyed(surface);
            }
            if (mVideoController != null) {
                mVideoController.onSurfaceDestroyed(surface);
            }
        }
    }

    private class InnerMediaEventListener implements MediaEventListener {

        @Override
        public void onInitialized() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onInitialized();
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(false);
            }
        }

        @Override
        public void onPreparing(boolean autoStart) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPreparing(autoStart);
            }
            if (autoStart && mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(true);
            }
        }

        @Override
        public void onPrepared(long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPrepared(duration);
            }
        }

        @Override
        public void onExecuteStart() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onExecuteStart();
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(true);
            }
        }

        @Override
        public void onStart(long currentPosition, long duration, int bufferingPercent) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onStart(currentPosition, duration, bufferingPercent);
            }
        }

        @Override
        public void onPlaying(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPlaying(currentPosition, duration);
            }
        }

        @Override
        public void onPause(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onPause(currentPosition, duration);
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(false);
            }
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onStop(currentPosition, duration);
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(false);
            }
        }

        @Override
        public void onBufferingStart() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onBufferingStart();
            }
        }

        @Override
        public void onBufferingEnd() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onBufferingEnd();
            }
        }

        @Override
        public void onBufferingUpdate(int percent) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onBufferingUpdate(percent);
            }
        }

        @Override
        public void onSeekStart(long seekMilliSeconds, int seekProgress) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onSeekStart(seekMilliSeconds, seekProgress);
            }
        }

        @Override
        public void onSeekEnd() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onSeekEnd();
            }
        }

        @Override
        public void onError(int what, int extra) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onError(what, extra);
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(false);
            }
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            if (mVideoSurface != null) {
                mVideoSurface.setVideoSize(width, height);
            }
            if (mVideoPreview != null) {
                mVideoPreview.getView().requestLayout();
            }
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onVideoSizeChanged(width, height);
            }
        }

        @Override
        public void onCompletion() {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onCompletion();
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(false);
            }
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            for (MediaEventListener listener : mMediaEventListeners) {
                listener.onRelease(currentPosition, duration);
            }
            if (mVideoContainer != null) {
                mVideoContainer.setKeepScreenOn(false);
            }
        }
    }

    private class InnerMediaProgressListener implements MediaProgressListener {

        @Override
        public void onMediaProgress(int progress, long currentPosition, long duration) {
            for (MediaProgressListener listener : mMediaProgressListeners) {
                listener.onMediaProgress(progress, currentPosition, duration);
            }
        }
    }


    private class WindowAttachListenerView extends View {

        public WindowAttachListenerView(Context context) {
            super(context);
            setBackgroundColor(Color.TRANSPARENT);
            setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            release();
        }
    }

    public interface Scene {
        int NORMAL = 0;
        int FULLSCREEN = 1;
        //int TINY = 2;
    }
}
