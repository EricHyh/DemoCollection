package com.hyh.video.lib;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */
public class DefaultVideoController implements IVideoController {

    private static final String TAG = "DefaultVideoController";

    //
    private static final int INTERCEPT_NONE = 0;
    private static final int INTERCEPT_PREPARE = 1;
    private static final int INTERCEPT_START = 2;
    private static final int INTERCEPT_RESTART = 3;
    private static final int INTERCEPT_RETRY = 4;
    //

    //
    private static final int CONTROL_STATE_INITIAL = 0;
    private static final int CONTROL_STATE_MOBILE_DATA_CONFIRM = 1;
    private static final int CONTROL_STATE_OPERATE = 2;
    private static final int CONTROL_STATE_END = 3;
    private static final int CONTROL_STATE_ERROR = 4;


    private static boolean sAllowPlayWhenMobileData;

    private final HideOperateViewTask mHideOperateViewTask = new HideOperateViewTask(this);
    private final SurfaceDestroyTask mSurfaceDestroyTask = new SurfaceDestroyTask(this);
    private final MediaEventListener mControllerMediaEventListener = new ControllerMediaEventListener();
    private final MediaProgressListener mControllerMediaProgressListener = new ControllerMediaProgressListener();
    private final Context mContext;
    private final IControllerView mControllerView;
    private VideoDelegate mVideoDelegate;

    private int mCurInterceptCommand = INTERCEPT_NONE;
    private int mCurControlState = CONTROL_STATE_INITIAL;
    private boolean mInterceptPrepareAutoStart;
    private long mSurfaceDestroyedTimeMillis;
    private boolean mIsPauseBySurfaceDestroyed;


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
    public void setup(VideoDelegate videoDelegate, CharSequence title, IMediaInfo mediaInfo) {
        this.mVideoDelegate = videoDelegate;
        this.mIsPauseBySurfaceDestroyed = false;
        this.mSurfaceDestroyedTimeMillis = 0;
        Log.d(TAG, "setup: ");
        mVideoDelegate.addMediaEventListener(mControllerMediaEventListener);
        mVideoDelegate.addMediaProgressListener(mControllerMediaProgressListener);

        mControllerView.setup(videoDelegate, title, mediaInfo);
        mControllerView.setControllerViewClickListener(new ControllerClickListener(ControllerClickListener.FLAG_CONTROLLER_VIEW));
        mControllerView.setPlayOrPauseClickListener(new ControllerClickListener(ControllerClickListener.FLAG_PLAY_OR_PAUSE));
        mControllerView.setReplayClickListener(new ControllerClickListener(ControllerClickListener.FLAG_REPLAY));
        mControllerView.setRetryClickListener(new ControllerClickListener(ControllerClickListener.FLAG_RETRY));
        mControllerView.setFullScreenToggleClickListener(new ControllerClickListener(ControllerClickListener.FLAG_FULLSCREEN_TOGGLE));
        mControllerView.setMobileDataConfirmClickListener(new ControllerClickListener(ControllerClickListener.FLAG_MOBILE_DATA_CONFIRM));
        mControllerView.setFullscreenBackClickListener(new ControllerClickListener(ControllerClickListener.FLAG_FULLSCREEN_BACK));
        mControllerView.setOnSeekBarChangeListener(new ControllerSeekBarChangeListener());

        mHideOperateViewTask.remove();
        mControllerView.showInitialView();
        mCurControlState = CONTROL_STATE_INITIAL;
    }

    @Override
    public boolean interceptPrepare(boolean autoStart) {
        if (!VideoUtils.isNetEnv(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!sAllowPlayWhenMobileData && !VideoUtils.isWifiEnv(mContext)) {
            this.mInterceptPrepareAutoStart = autoStart;
            mCurInterceptCommand = INTERCEPT_PREPARE;
            mControllerView.showMobileDataConfirm();
            return true;
        }
        return false;
    }

    @Override
    public boolean interceptStart() {
        if (!VideoUtils.isNetEnv(mContext)) {
            Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!sAllowPlayWhenMobileData && !VideoUtils.isWifiEnv(mContext)) {
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
        if (!sAllowPlayWhenMobileData && !VideoUtils.isWifiEnv(mContext)) {
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
        if (!sAllowPlayWhenMobileData && !VideoUtils.isWifiEnv(mContext)) {
            mCurInterceptCommand = INTERCEPT_RETRY;
            mControllerView.showMobileDataConfirm();
            return true;
        }
        return false;
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
        }
        mVideoDelegate.pause();
        mSurfaceDestroyedTimeMillis = System.currentTimeMillis();
    }


    private class ControllerSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
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
            mCurControlState = CONTROL_STATE_OPERATE;
        }

        @Override
        public void onStart(long currentPosition, long duration) {
        }

        @Override
        public void onPlaying(long currentPosition, long duration) {
            mControllerView.hideLoadingView();
            mCurControlState = CONTROL_STATE_OPERATE;
        }

        @Override
        public void onPause(long currentPosition, long duration) {
            mControllerView.setPlayStyle();
            mHideOperateViewTask.remove();
            mControllerView.hideLoadingView();
            mControllerView.showOperateView();
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            mControllerView.setMediaProgress(0);
            mControllerView.setCurrentPosition(0);
            mHideOperateViewTask.remove();
            mControllerView.hideLoadingView();
            mControllerView.showInitialView();
            mCurControlState = CONTROL_STATE_INITIAL;
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
        public void onBufferingUpdate(int progress) {
            mControllerView.setBufferingProgress(progress);
        }

        @Override
        public void onSeekStart(long seekMilliSeconds, int seekProgress) {
            mControllerView.setMediaProgress(seekProgress);
            mControllerView.setCurrentPosition(seekMilliSeconds);
        }

        @Override
        public void onSeekEnd() {
        }

        @Override
        public void onError(int what, int extra) {
            mControllerView.showErrorView();
            mControllerView.hideLoadingView();
            mCurControlState = CONTROL_STATE_ERROR;
        }

        @Override
        public void onCompletion() {
            mHideOperateViewTask.remove();
            mControllerView.showEndView();
            mCurControlState = CONTROL_STATE_END;
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            mHideOperateViewTask.remove();
            mControllerView.showInitialView();
            mCurControlState = CONTROL_STATE_INITIAL;
        }
    }

    private class ControllerMediaProgressListener implements MediaProgressListener {

        @Override
        public void onMediaProgress(int progress, long currentPosition, long duration) {
            mControllerView.setMediaProgress(progress);
            mControllerView.setCurrentPosition(currentPosition);
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
                    mHideOperateViewTask.remove();
                    if (mControllerView.isShowOperateView()) {
                        mControllerView.hideOperateView();
                    } else {
                        mControllerView.showOperateView();
                        if (mVideoDelegate.isExecuteStart()) {
                            mHideOperateViewTask.post();
                        }
                    }
                    break;
                }
            }
        }

        private void handleStartIconClick() {
            mHideOperateViewTask.remove();
            if (mVideoDelegate.isExecuteStart()) {
                mVideoDelegate.pause();
                mControllerView.setPlayStyle();
            } else {
                if (!VideoUtils.isNetEnv(mContext)) {
                    Toast.makeText(mContext, "网络不可用", Toast.LENGTH_SHORT).show();
                } else {
                    mVideoDelegate.start();
                    mControllerView.setPauseStyle();
                    mHideOperateViewTask.post();
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
            defaultVideoController.mHideOperateViewTask.remove();
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

    private static class HideOperateViewTask implements Runnable {

        private WeakReference<DefaultVideoController> mDefaultVideoControllerRef;

        HideOperateViewTask(DefaultVideoController defaultVideoController) {
            mDefaultVideoControllerRef = new WeakReference<>(defaultVideoController);
        }

        @Override
        public void run() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            defaultVideoController.mControllerView.hideOperateView();
        }

        void post() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            VideoUtils.postUiThreadDelayed(this, 3000);
        }

        void remove() {
            DefaultVideoController defaultVideoController = mDefaultVideoControllerRef.get();
            if (defaultVideoController == null) return;
            VideoUtils.removeUiThreadRunnable(this);
        }
    }
}