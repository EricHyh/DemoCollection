package com.hyh.video.lib;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/3/18
 */

public class VideoDelegate {

    private static final String TAG = "VideoDelegate";

    private final IVideoSurface.SurfaceListener mSurfaceListener = new InnerSurfaceListener();
    private final MediaEventListener mMediaEventListener = new InnerMediaEventListener();
    private final MediaProgressListener mMediaProgressListener = new InnerMediaProgressListener();
    private final List<MediaEventListener> mMediaEventListeners = new CopyOnWriteArrayList<>();
    private final List<MediaProgressListener> mMediaProgressListeners = new CopyOnWriteArrayList<>();
    private final List<IVideoSurface.SurfaceListener> mSurfaceListeners = new CopyOnWriteArrayList<>();
    private final SceneChangeHelper mSceneChangeHelper;
    private final WindowAttachListenerView mWindowAttachListenerView;

    protected final IMediaPlayer mMediaPlayer = new MediaSystem();
    protected final Context mContext;
    protected final IMediaInfo mMediaInfo;

    protected ISurfaceMeasurer mSurfaceMeasurer = new FitCenterMeasurer();
    protected IVideoBackground mVideoBackground;
    protected IVideoSurface mVideoSurface;
    protected IVideoPreview mVideoPreview;
    protected IVideoController mVideoController;

    protected FrameLayout mNormalVideoContainer;
    protected FrameLayout mVideoContainer;

    protected CharSequence mTitle;

    public VideoDelegate(Context context) {
        this.mContext = context;
        this.mSceneChangeHelper = new SceneChangeHelper(context);
        this.mWindowAttachListenerView = new WindowAttachListenerView(context);

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
        VideoManager.getInstance().addVideo(this);
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
            mVideoSurface.onVideoSceneChanged(videoContainer, mSceneChangeHelper.mScene);
        }
        if (mVideoPreview != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoContainer.addView(mVideoPreview.getView(), params);
        }
        if (mVideoController != null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoContainer.addView(mVideoController.getView(), params);
            mVideoSurface.onVideoSceneChanged(videoContainer, mSceneChangeHelper.mScene);
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
        return new TextureVideo(context);
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

    public void setFullscreenActivity(Activity fullscreenActivity) {
        mSceneChangeHelper.mFullscreenActivity = fullscreenActivity;
    }

    public void setFullscreenAllowLandscape(boolean fullscreenAllowLandscape) {
        mSceneChangeHelper.mFullscreenAllowLandscape =
                fullscreenAllowLandscape;
    }

    public void setFullscreenAllowRotate(boolean fullscreenAllowRotate) {
        mSceneChangeHelper.mFullscreenAllowRotate = fullscreenAllowRotate;
    }

    public int getScene() {
        return mSceneChangeHelper.mScene;
    }

    public boolean startFullscreenScene() {
        return mSceneChangeHelper.startFullscreenScene();
    }

    public boolean recoverNormalScene() {
        return mSceneChangeHelper.recoverNormalScene();
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
    }

    public void start() {
        if (mVideoController != null && mVideoController.interceptStart()) return;
        mMediaPlayer.start();
    }

    public void restart() {
        if (mVideoController != null && mVideoController.interceptRestart()) return;
        mMediaPlayer.restart();
    }

    public void retry() {
        if (mVideoController != null && mVideoController.interceptRetry()) return;
        mMediaPlayer.retry();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public void release() {
        mMediaPlayer.release();
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
            mSceneChangeHelper.onVideoRelease();
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
        int TINY = 2;
    }

    class SceneChangeHelper implements OrientationManager.OrientationChangedListener {

        private static final int FULLSCREEN_KEEP_ORIENTATION = 1;
        private static final int FULLSCREEN_ACTIVITY_LANDSCAPE = 2;
        private static final int FULLSCREEN_ACTIVITY_REVERSE_LANDSCAPE = 3;
        private static final int FULLSCREEN_VIEW_LANDSCAPE = 4;
        private static final int FULLSCREEN_VIEW_REVERSE_LANDSCAPE = 5;

        final Context context;

        int mScene = Scene.NORMAL;
        Activity mFullscreenActivity;
        boolean mFullscreenAllowLandscape = true;
        boolean mFullscreenAllowRotate = true;

        int mFullscreenMode;
        int mNormalSceneScreenOrientation;

        SceneChangeHelper(Context context) {
            this.context = context;
            OrientationManager.getInstance(context).addOrientationChangedListener(this);
        }

        boolean startFullscreenScene() {
            if (mScene == Scene.FULLSCREEN) return false;
            mScene = Scene.FULLSCREEN;
            ViewGroup rootView = (ViewGroup) mVideoContainer.getRootView();
            mNormalVideoContainer = mVideoContainer;
            detachedFromContainer();
            HappyVideo happyVideo = new HappyVideo(mContext, null, 0, VideoDelegate.this);

            Activity activity = getActivity();
            mNormalSceneScreenOrientation = VideoUtils.getScreenOrientation(mContext);
            mFullscreenMode = getFullscreenMode(activity);

            switch (mFullscreenMode) {
                case FULLSCREEN_KEEP_ORIENTATION: {
                    rootView.addView(happyVideo, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    break;
                }
                case FULLSCREEN_ACTIVITY_LANDSCAPE: {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    rootView.addView(happyVideo, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    break;
                }
                case FULLSCREEN_ACTIVITY_REVERSE_LANDSCAPE: {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    rootView.addView(happyVideo, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    break;
                }
                case FULLSCREEN_VIEW_LANDSCAPE: {
                    happyVideo.setRotation(90);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rootView.getMeasuredHeight(), rootView.getMeasuredWidth());
                    layoutParams.gravity = Gravity.CENTER;
                    rootView.addView(happyVideo, layoutParams);
                    break;
                }
                case FULLSCREEN_VIEW_REVERSE_LANDSCAPE: {
                    happyVideo.setRotation(270);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rootView.getMeasuredHeight(), rootView.getMeasuredWidth());
                    layoutParams.gravity = Gravity.CENTER;
                    rootView.addView(happyVideo, layoutParams);
                    break;
                }
            }
            return true;
        }

        boolean recoverNormalScene() {
            if (mScene == Scene.NORMAL) return false;
            mScene = Scene.NORMAL;
            ViewGroup parent = (ViewGroup) mVideoContainer.getParent();
            if (parent != null) {
                parent.removeView(mVideoContainer);
            }

            detachedFromContainer();
            attachedToContainer(mNormalVideoContainer);
            mNormalVideoContainer = null;

            Activity activity = getActivity();

            switch (mFullscreenMode) {
                case FULLSCREEN_KEEP_ORIENTATION: {
                    break;
                }
                case FULLSCREEN_ACTIVITY_LANDSCAPE: {
                    activity.setRequestedOrientation(mNormalSceneScreenOrientation);
                    break;
                }
                case FULLSCREEN_ACTIVITY_REVERSE_LANDSCAPE: {
                    activity.setRequestedOrientation(mNormalSceneScreenOrientation);
                    break;
                }
                case FULLSCREEN_VIEW_LANDSCAPE: {
                    mVideoContainer.setRotation(0);
                    break;
                }
                case FULLSCREEN_VIEW_REVERSE_LANDSCAPE: {
                    mVideoContainer.setRotation(0);
                    break;
                }
            }
            return true;
        }

        int getFullscreenMode(Activity activity) {
            if (mFullscreenAllowLandscape) {
                if (VideoUtils.isActivitySupportChangeOrientation(activity)) {
                    int currentOrientation = OrientationManager.getInstance(mContext).getCurrentOrientation();
                    if (currentOrientation == OrientationManager.ORIENTATION_REVERSE_LANDSCAPE) {
                        return FULLSCREEN_ACTIVITY_REVERSE_LANDSCAPE;
                    } else {
                        return FULLSCREEN_ACTIVITY_LANDSCAPE;
                    }
                } else {
                    if (mFullscreenAllowRotate && mVideoSurface.isSupportRotate()) {
                        int screenOrientation = VideoUtils.getScreenOrientation(mContext);
                        if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                || screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                            int currentOrientation = OrientationManager.getInstance(mContext).getCurrentOrientation();
                            if (currentOrientation == OrientationManager.ORIENTATION_REVERSE_LANDSCAPE) {
                                return FULLSCREEN_VIEW_REVERSE_LANDSCAPE;
                            } else {
                                return FULLSCREEN_VIEW_LANDSCAPE;
                            }
                        } else {
                            return FULLSCREEN_KEEP_ORIENTATION;
                        }
                    } else {
                        return FULLSCREEN_KEEP_ORIENTATION;
                    }
                }
            } else {
                return FULLSCREEN_KEEP_ORIENTATION;
            }
        }

        public Activity getActivity() {
            if (mFullscreenActivity != null) return mFullscreenActivity;
            if (mContext instanceof Activity) return ((Activity) mContext);
            return null;
        }

        @Override
        public void onChanged(int oldOrientation, int newOrientation) {
            if (mScene == Scene.FULLSCREEN) {
                if (mFullscreenMode == FULLSCREEN_ACTIVITY_LANDSCAPE
                        || mFullscreenMode == FULLSCREEN_ACTIVITY_REVERSE_LANDSCAPE) {
                    if (newOrientation == OrientationManager.ORIENTATION_LANDSCAPE
                            || newOrientation == OrientationManager.ORIENTATION_REVERSE_LANDSCAPE) {
                        Activity activity = getActivity();
                        activity.setRequestedOrientation(newOrientation);
                    } else if (newOrientation == OrientationManager.ORIENTATION_PORTRAIT) {
                        if (VideoUtils.isAccelerometerRotationOpened(mContext)) {
                            recoverNormalScene();
                        }
                    }
                } else if (mFullscreenMode == FULLSCREEN_VIEW_LANDSCAPE
                        || mFullscreenMode == FULLSCREEN_VIEW_REVERSE_LANDSCAPE) {
                    if (newOrientation == OrientationManager.ORIENTATION_LANDSCAPE) {
                        mVideoContainer.setRotation(90);
                    } else if (newOrientation == OrientationManager.ORIENTATION_REVERSE_LANDSCAPE) {
                        mVideoContainer.setRotation(270);
                    } else if (newOrientation == OrientationManager.ORIENTATION_PORTRAIT) {
                        if (VideoUtils.isAccelerometerRotationOpened(mContext)) {
                            recoverNormalScene();
                        }
                    }
                }
            } else if (mScene == Scene.NORMAL) {
                if (mFullscreenAllowLandscape &&
                        mMediaPlayer.isExecuteStart() &&
                        (VideoUtils.isAccelerometerRotationOpened(mContext))) {
                    if (newOrientation == OrientationManager.ORIENTATION_LANDSCAPE
                            || newOrientation == OrientationManager.ORIENTATION_REVERSE_LANDSCAPE) {
                        Activity activity = getActivity();
                        if (VideoUtils.isActivitySupportChangeOrientation(activity) || mFullscreenAllowRotate) {
                            startFullscreenScene();
                        }
                    }
                }
            }
        }

        void onVideoRelease() {
            OrientationManager.getInstance(context).removeOrientationChangedListener(this);
        }
    }
}