package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */
public class DefaultVideoController implements IVideoController {

    protected static final String TAG = "DefaultVideoController";

    //
    protected static final int INTERCEPT_NONE = 0;
    protected static final int INTERCEPT_PREPARE = 1;
    protected static final int INTERCEPT_START = 2;
    protected static final int INTERCEPT_RESTART = 3;
    protected static final int INTERCEPT_RETRY = 4;
    //

    //
    protected static final int CONTROL_STATE_INITIAL = 0;
    protected static final int CONTROL_STATE_MOBILE_DATA_CONFIRM = 1;
    protected static final int CONTROL_STATE_OPERATE = 2;
    protected static final int CONTROL_STATE_END = 3;
    protected static final int CONTROL_STATE_ERROR = 4;


    protected static boolean sAllowPlayWhenMobileData;

    protected final IdleOperateViewTask mIdleOperateViewTask = new IdleOperateViewTask(this);
    protected final SurfaceDestroyTask mSurfaceDestroyTask = new SurfaceDestroyTask(this);
    protected final MediaEventListener mControllerMediaEventListener = new ControllerMediaEventListener();
    protected final VideoPositionHelper mVideoPositionHelper = new VideoPositionHelper();

    protected final Context mContext;
    protected final IControllerView mControllerView;
    protected VideoDelegate mVideoDelegate;

    protected int mCurInterceptCommand = INTERCEPT_NONE;
    protected int mCurControlState = CONTROL_STATE_INITIAL;
    protected boolean mInterceptPrepareAutoStart;
    protected long mSurfaceDestroyedTimeMillis;
    protected boolean mIsPauseBySurfaceDestroyed;


    public DefaultVideoController(Context context) {
        this.mContext = context;
        this.mControllerView = new DefaultControllerView(context);

    }

    public DefaultVideoController(Context context, IControllerView controllerView) {
        this.mContext = context;
        this.mControllerView = controllerView;
    }

    @Override
    public View getView() {
        return mControllerView.getView();
    }

    @Override
    public void setup(VideoDelegate videoDelegate, CharSequence title, long playCount, IMediaInfo mediaInfo) {
        this.mVideoDelegate = videoDelegate;
        this.mIsPauseBySurfaceDestroyed = false;
        this.mSurfaceDestroyedTimeMillis = 0;

        mVideoDelegate.addMediaEventListener(mControllerMediaEventListener);
        mVideoDelegate.addMediaProgressListener(mVideoPositionHelper);

        mControllerView.setup(videoDelegate, title, playCount, mediaInfo);

        mControllerView.setControllerViewTouchListener(new ControllerTouchListener());
        mControllerView.setControllerViewClickListener(new ControllerClickListener(ControllerClickListener.FLAG_CONTROLLER_VIEW));
        mControllerView.setPlayOrPauseClickListener(new ControllerClickListener(ControllerClickListener.FLAG_PLAY_OR_PAUSE));
        mControllerView.setReplayClickListener(new ControllerClickListener(ControllerClickListener.FLAG_REPLAY));
        mControllerView.setRetryClickListener(new ControllerClickListener(ControllerClickListener.FLAG_RETRY));
        mControllerView.setFullScreenToggleClickListener(new ControllerClickListener(ControllerClickListener.FLAG_FULLSCREEN_TOGGLE));
        mControllerView.setMobileDataConfirmClickListener(new ControllerClickListener(ControllerClickListener.FLAG_MOBILE_DATA_CONFIRM));
        mControllerView.setFullscreenBackClickListener(new ControllerClickListener(ControllerClickListener.FLAG_FULLSCREEN_BACK));
        mControllerView.setOnSeekBarChangeListener(mVideoPositionHelper);

        mIdleOperateViewTask.remove();
        mControllerView.showInitialView();
        mCurControlState = CONTROL_STATE_INITIAL;
    }

    @Override
    public boolean interceptPrepare(boolean autoStart) {
        if (!VideoUtils.isNetEnv(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!isAllowPlayWhenMobileData() && !VideoUtils.isWifiEnv(mContext)) {
            this.mInterceptPrepareAutoStart = autoStart;
            mCurInterceptCommand = INTERCEPT_PREPARE;
            mControllerView.showMobileDataConfirm();
            return true;
        }
        return false;
    }

    public boolean interceptStart() {
        if (!VideoUtils.isNetEnv(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!isAllowPlayWhenMobileData() && !VideoUtils.isWifiEnv(mContext)) {
            mCurInterceptCommand = INTERCEPT_START;
            mControllerView.showMobileDataConfirm();
            mCurControlState = CONTROL_STATE_MOBILE_DATA_CONFIRM;
            return true;
        }
        return false;
    }

    @Override
    public boolean interceptRestart() {
        if (!VideoUtils.isNetEnv(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!isAllowPlayWhenMobileData() && !VideoUtils.isWifiEnv(mContext)) {
            mCurInterceptCommand = INTERCEPT_RESTART;
            mControllerView.showMobileDataConfirm();
            mCurControlState = CONTROL_STATE_MOBILE_DATA_CONFIRM;
            return true;
        }
        return false;
    }

    @Override
    public boolean interceptRetry() {
        if (!VideoUtils.isNetEnv(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!isAllowPlayWhenMobileData() && !VideoUtils.isWifiEnv(mContext)) {
            mCurInterceptCommand = INTERCEPT_RETRY;
            mControllerView.showMobileDataConfirm();
            return true;
        }
        return false;
    }

    @Override
    public void onVideoSceneChanged(FrameLayout videoContainer, int scene) {
        if (scene == VideoDelegate.Scene.FULLSCREEN) {
            onFullscreenScene(videoContainer);
        } else if (scene == VideoDelegate.Scene.NORMAL) {
            onNormalScene(videoContainer);
        }
        mControllerView.onVideoSceneChanged(videoContainer, scene);
    }

    @Override
    public boolean isFullScreenLocked() {
        return mControllerView.isFullScreenLocked();
    }

    @Override
    public void onBackPress() {
        if (!mControllerView.isFullScreenLocked()) {
            mVideoDelegate.recoverNormalScene();
        } else {
            mControllerView.showToast("请先解锁");
        }
    }

    private void onFullscreenScene(final FrameLayout videoContainer) {
        //mControllerView
        int flags = videoContainer.getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        videoContainer.setSystemUiVisibility(flags);
        videoContainer.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (mVideoDelegate.getScene() == VideoDelegate.Scene.FULLSCREEN) {
                    int systemUiVisibility = videoContainer.getSystemUiVisibility();
                    if (visibility == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_STABLE) == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_IMMERSIVE) == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == 0 ||
                            (systemUiVisibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                        videoContainer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int flags = videoContainer.getSystemUiVisibility()
                                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                                videoContainer.setSystemUiVisibility(flags);
                            }
                        }, 3000);
                    }
                }
            }
        });
    }

    private void onNormalScene(FrameLayout videoContainer) {
    }

    protected void showOperateView(int mode) {
        mControllerView.showOperateView(mode);
        if (mVideoDelegate.getScene() == VideoDelegate.Scene.FULLSCREEN) {
            /*if (mode == IControllerView.OperateMode.ALIVE) {
                int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                mControllerView.getView().setSystemUiVisibility(flags);
            } else {
                int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                mControllerView.getView().setSystemUiVisibility(flags);
            }*/
        }
    }

    protected boolean isAllowPlayWhenMobileData() {
        return sAllowPlayWhenMobileData;
    }

    @Override
    public void onSurfaceCreate(Surface surface) {
        Log.d(TAG, "onSurfaceCreate: ");
        mSurfaceDestroyTask.remove();
        if (mIsPauseBySurfaceDestroyed) {
            long currentTimeMillis = System.currentTimeMillis();
            long timeInterval = Math.abs(currentTimeMillis - mSurfaceDestroyedTimeMillis);
            if (timeInterval < 300) {
                mVideoDelegate.start();
            }
        }
        mIsPauseBySurfaceDestroyed = false;
        mSurfaceDestroyedTimeMillis = 0;
    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroyed(Surface surface) {
        Log.d(TAG, "onSurfaceDestroyed: ");
        mSurfaceDestroyTask.post();
        if (mVideoDelegate.isExecuteStart()) {
            mIsPauseBySurfaceDestroyed = true;
            mVideoDelegate.pause();
        }
        mSurfaceDestroyedTimeMillis = System.currentTimeMillis();
    }

    private class VideoPositionHelper implements MediaProgressListener, SeekBar.OnSeekBarChangeListener {

        private boolean mIsStartTrackingTouch;

        void onStop(long currentPosition, long duration) {
            mControllerView.setMediaProgress(0);
            mControllerView.setCurrentPosition(0);
        }

        void onSeekStart(long seekMilliSeconds, int seekProgress) {
            mControllerView.setMediaProgress(seekProgress);
            mControllerView.setCurrentPosition(seekMilliSeconds);
        }

        void onSeekEnd() {
        }

        @Override
        public void onMediaProgress(int progress, long currentPosition, long duration) {
            if (!mIsStartTrackingTouch) {
                mControllerView.setMediaProgress(progress);
                mControllerView.setCurrentPosition(currentPosition);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                long currentPosition = Math.round(mVideoDelegate.getDuration() * progress * 1.0 / 100);
                mControllerView.setCurrentPosition(currentPosition);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mIsStartTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mIsStartTrackingTouch = false;
            mVideoDelegate.seekProgressTo(seekBar.getProgress());
        }
    }

    private class ControllerMediaEventListener extends SimpleMediaEventListener {

        @Override
        public void onPreparing(boolean autoStart) {
            if (autoStart || mVideoDelegate.isExecuteStart()) {
                mControllerView.showLoadingViewDelayed(300);
                mCurControlState = CONTROL_STATE_OPERATE;
            }
        }

        @Override
        public void onPrepared(long duration) {
            mControllerView.setDuration(duration);
        }

        @Override
        public void onExecuteStart() {
            mControllerView.hideInitialView();
            mControllerView.hideEndView();
            mControllerView.hideErrorView();
            mControllerView.hideMobileDataConfirm();

            mControllerView.showLoadingViewDelayed(300);
            mControllerView.setPauseStyle();
            showOperateView(IControllerView.OperateMode.IDLE);
            mCurControlState = CONTROL_STATE_OPERATE;
        }

        @Override
        public void onStart(long currentPosition, long duration, int bufferingPercent) {
            mControllerView.hideLoadingView();
            mCurControlState = CONTROL_STATE_OPERATE;

            Log.d(TAG, "onStart: " + mVideoDelegate.isPlaying());

        }

        @Override
        public void onPlaying(long currentPosition, long duration) {
            mControllerView.hideLoadingView();
            mCurControlState = CONTROL_STATE_OPERATE;
        }

        @Override
        public void onPause(long currentPosition, long duration) {
            mControllerView.setPlayStyle();
            mIdleOperateViewTask.remove();
            mControllerView.hideLoadingView();
            showOperateView(IControllerView.OperateMode.ALIVE);
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            mIdleOperateViewTask.remove();
            mControllerView.hideLoadingView();
            mControllerView.showInitialView();
            mCurControlState = CONTROL_STATE_INITIAL;

            mVideoPositionHelper.onStop(currentPosition, duration);
        }

        @Override
        public void onBufferingStart() {
            if (mCurControlState != CONTROL_STATE_INITIAL) {
                mControllerView.showLoadingView();
            }
        }

        @Override
        public void onBufferingEnd() {
            mControllerView.hideLoadingView();
        }

        @Override
        public void onBufferingUpdate(int percent) {
            mControllerView.setBufferingProgress(percent);
        }

        @Override
        public void onSeekStart(long seekMilliSeconds, int seekProgress) {
            mVideoPositionHelper.onSeekStart(seekMilliSeconds, seekProgress);
        }

        @Override
        public void onSeekEnd() {
            mVideoPositionHelper.onSeekEnd();
        }

        @Override
        public void onError(int what, int extra) {
            mControllerView.showErrorView();
            mControllerView.hideLoadingView();
            mCurControlState = CONTROL_STATE_ERROR;
        }

        @Override
        public void onCompletion() {
            mIdleOperateViewTask.remove();
            mControllerView.showEndView();
            mCurControlState = CONTROL_STATE_END;
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            mIdleOperateViewTask.remove();
            mControllerView.showInitialView();
            mCurControlState = CONTROL_STATE_INITIAL;
        }
    }

    private class ControllerTouchListener implements View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    if (mControllerView.isShowOperateView()) {
                        mIdleOperateViewTask.remove();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (mControllerView.isShowOperateView()) {
                        mIdleOperateViewTask.post();
                    }
                    break;
                }
            }
            return false;
        }
    }

    private class ControllerClickListener implements View.OnClickListener {

        private static final int FLAG_CONTROLLER_VIEW = 1;
        private static final int FLAG_PLAY_OR_PAUSE = 2;
        private static final int FLAG_REPLAY = 3;
        private static final int FLAG_RETRY = 4;
        private static final int FLAG_FULLSCREEN_TOGGLE = 5;
        private static final int FLAG_MOBILE_DATA_CONFIRM = 6;
        private static final int FLAG_FULLSCREEN_BACK = 7;

        private final int flag;

        ControllerClickListener(int flag) {
            this.flag = flag;
        }

        @Override
        public void onClick(View v) {
            switch (flag) {
                case FLAG_CONTROLLER_VIEW: {
                    handleControllerViewClick();
                    break;
                }
                case FLAG_PLAY_OR_PAUSE: {
                    handleStartIconClick();
                    break;
                }
                case FLAG_REPLAY: {
                    handleReplayIconCLick();
                    break;
                }
                case FLAG_RETRY: {
                    handleRetryButtonClick();
                    break;
                }
                case FLAG_FULLSCREEN_TOGGLE: {
                    handleFullscreenToggleClick();
                    break;
                }
                case FLAG_MOBILE_DATA_CONFIRM: {
                    handleMobileDataConfirmClick();
                    break;
                }
                case FLAG_FULLSCREEN_BACK: {
                    handleFullscreenBackClick();
                    break;
                }
            }
        }

        private void handleControllerViewClick() {
            switch (mCurControlState) {
                case CONTROL_STATE_INITIAL: {
                    if (!VideoUtils.isNetEnv(mContext)) {
                        Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
                    } else {
                        mVideoDelegate.start();
                    }
                    break;
                }
                case CONTROL_STATE_OPERATE: {
                    mIdleOperateViewTask.remove();
                    if (mControllerView.isShowOperateView()) {
                        showOperateView(IControllerView.OperateMode.IDLE);
                    } else {
                        showOperateView(IControllerView.OperateMode.ALIVE);
                        if (mVideoDelegate.isExecuteStart()) {
                            mIdleOperateViewTask.post();
                        }
                    }
                    break;
                }
            }
        }

        private void handleStartIconClick() {
            mIdleOperateViewTask.remove();
            if (mVideoDelegate.isExecuteStart()) {
                mVideoDelegate.pause();
                mControllerView.setPlayStyle();
            } else {
                if (!VideoUtils.isNetEnv(mContext)) {
                    Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
                } else {
                    mVideoDelegate.start();
                    mControllerView.setPauseStyle();
                    mIdleOperateViewTask.post();
                }
            }
        }

        private void handleReplayIconCLick() {
            if (!VideoUtils.isNetEnv(mContext)) {
                Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            } else {
                mVideoDelegate.restart();
                mControllerView.hideEndView();
            }
        }


        private void handleRetryButtonClick() {
            if (!VideoUtils.isNetEnv(mContext)) {
                Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            } else {
                mVideoDelegate.retry();
                mControllerView.hideErrorView();
            }
        }

        private void handleFullscreenToggleClick() {
            if (mVideoDelegate.getScene() == VideoDelegate.Scene.NORMAL) {
                mVideoDelegate.startFullscreenScene();
            } else {
                mVideoDelegate.recoverNormalScene();
            }
        }

        private void handleMobileDataConfirmClick() {
            sAllowPlayWhenMobileData = true;
            switch (mCurInterceptCommand) {
                case INTERCEPT_PREPARE: {
                    mVideoDelegate.prepare(mInterceptPrepareAutoStart);
                    break;
                }
                case INTERCEPT_START: {
                    mVideoDelegate.start();
                    break;
                }
                case INTERCEPT_RESTART: {
                    mVideoDelegate.restart();
                    break;
                }
                case INTERCEPT_RETRY: {
                    mVideoDelegate.retry();
                    break;
                }
            }
            mControllerView.hideMobileDataConfirm();
        }

        private void handleFullscreenBackClick() {
            if (mVideoDelegate.getScene() == VideoDelegate.Scene.FULLSCREEN) {
                mVideoDelegate.recoverNormalScene();
            }
        }
    }

    private static class SurfaceDestroyTask implements Runnable {

        private WeakReference<DefaultVideoController> mDefaultVideoControllerRef;

        SurfaceDestroyTask(DefaultVideoController defaultVideoController) {
            mDefaultVideoControllerRef = new WeakReference<>(defaultVideoController);
        }

        @Override
        public void run() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            defaultVideoController.mIdleOperateViewTask.remove();
            defaultVideoController.mControllerView.showInitialView();
            defaultVideoController.mCurControlState = CONTROL_STATE_INITIAL;
        }

        void post() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            VideoUtils.postUiThreadDelayed(this, 300);
        }

        void remove() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            VideoUtils.removeUiThreadRunnable(this);
        }
    }

    private static class IdleOperateViewTask implements Runnable {

        private WeakReference<DefaultVideoController> mDefaultVideoControllerRef;

        IdleOperateViewTask(DefaultVideoController defaultVideoController) {
            mDefaultVideoControllerRef = new WeakReference<>(defaultVideoController);
        }

        @Override
        public void run() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            defaultVideoController.showOperateView(IControllerView.OperateMode.IDLE);
        }

        void post() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            VideoUtils.postUiThreadDelayed(this, 3666);
        }

        void remove() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            VideoUtils.removeUiThreadRunnable(this);
        }
    }

}