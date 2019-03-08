package com.hyh.video.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */
public class DefaultVideoController implements IVideoController {


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

    private final MediaEventListener mControllerMediaEventListener = new ControllerMediaEventListener();
    private final MediaProgressListener mControllerMediaProgressListener = new ControllerMediaProgressListener();
    private final IVideoSurface.SurfaceListener mControllerSurfaceListener = new ControllerSurfaceListener();
    private final Context mContext;
    private final IControllerView mControllerView;
    private HappyVideo mHappyVideo;

    private int mCurInterceptCommand = INTERCEPT_NONE;
    private int mCurControlState = CONTROL_STATE_INITIAL;

    private boolean mInterceptPrepareAutoStart;


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
    public void setUp(HappyVideo happyVideo, CharSequence title, IMediaInfo mediaInfo) {
        this.mHappyVideo = happyVideo;
        mHappyVideo.addMediaEventListener(mControllerMediaEventListener);
        mHappyVideo.addMediaProgressListener(mControllerMediaProgressListener);
        mHappyVideo.addSurfaceListener(mControllerSurfaceListener);

        mControllerView.setTitle(title);
        mControllerView.setControllerViewClickListener(new ControllerClickListener(ControllerClickListener.FLAG_CONTROLLER_VIEW));
        mControllerView.setStartIconClickListener(new ControllerClickListener(ControllerClickListener.FLAG_START_ICON));
        mControllerView.setReplayIconClickListener(new ControllerClickListener(ControllerClickListener.FLAG_REPLAY_ICON));
        mControllerView.setRetryIconClickListener(new ControllerClickListener(ControllerClickListener.FLAG_RETRY_ICON));
        mControllerView.setFullScreenToggleClickListener(new ControllerClickListener(ControllerClickListener.FLAG_FULLSCREEN_TOGGLE));
        mControllerView.setMobileDataConfirmIconClickListener(new ControllerClickListener(ControllerClickListener.FLAG_MOBILE_DATA_CONFIRM));
        mControllerView.setBackIconClickListener(new ControllerClickListener(ControllerClickListener.FLAG_BACK_ICON));


        mControllerView.showInitialView(happyVideo.getDataSource());
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

    private class ControllerMediaEventListener extends SimpleMediaEventListener {

        @Override
        public void onPreparing() {
            mControllerView.showLoadingView();
        }

        @Override
        public void onPrepared(long duration) {
            mControllerView.setDuration(duration);
        }

        @Override
        public void onStart(long currentPosition, long duration) {
            mControllerView.setStartIconPauseStyle();
        }

        @Override
        public void onPlaying(long currentPosition, long duration) {
            mControllerView.hideLoadingView();
        }

        @Override
        public void onPause(long currentPosition, long duration) {
            mControllerView.setStartIconStartStyle();
        }

        @Override
        public void onStop(long currentPosition, long duration) {
            mControllerView.setMediaProgress(0);
            mControllerView.setCurrentPosition(0);
            mControllerView.showInitialView(mHappyVideo.getDataSource());
        }

        @Override
        public void onBufferingStart() {
            mControllerView.showLoadingView();
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
        }

        @Override
        public void onCompletion() {
            mControllerView.showEndView();
        }

        @Override
        public void onRelease(long currentPosition, long duration) {
            mControllerView.showInitialView(mHappyVideo.getDataSource());
        }
    }

    private class ControllerMediaProgressListener implements MediaProgressListener {

        @Override
        public void onMediaProgress(int progress, long currentPosition, long duration) {
            mControllerView.setMediaProgress(progress);
            mControllerView.setCurrentPosition(currentPosition);
        }
    }

    private class ControllerSurfaceListener implements IVideoSurface.SurfaceListener {

        @Override
        public void onSurfaceCreate(Surface surface) {
        }

        @Override
        public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        }

        @Override
        public void onSurfaceDestroyed(Surface surface) {
            mControllerView.showInitialView(mHappyVideo.getDataSource());
        }
    }

    private class ControllerClickListener implements View.OnClickListener {

        private static final int FLAG_CONTROLLER_VIEW = 1;
        private static final int FLAG_START_ICON = 2;
        private static final int FLAG_REPLAY_ICON = 3;
        private static final int FLAG_RETRY_ICON = 4;
        private static final int FLAG_FULLSCREEN_TOGGLE = 5;
        private static final int FLAG_MOBILE_DATA_CONFIRM = 6;
        private static final int FLAG_BACK_ICON = 7;


        private final HideOperateViewTask mHideOperateViewTask = new HideOperateViewTask();
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
                case FLAG_START_ICON: {
                    handleStartIconClick();
                    break;
                }
                case FLAG_REPLAY_ICON: {
                    mHappyVideo.restart();
                    break;
                }
                case FLAG_RETRY_ICON: {
                    mHappyVideo.retry();
                    break;
                }
                case FLAG_FULLSCREEN_TOGGLE: {
                    //TODO 暂不实现
                    break;
                }
                case FLAG_MOBILE_DATA_CONFIRM: {
                    handleMobileDataConfirmClick();
                    break;
                }
                case FLAG_BACK_ICON: {
                    //TODO 暂不实现
                    break;
                }
            }
        }

        private void handleControllerViewClick() {
            switch (mCurControlState) {
                case CONTROL_STATE_INITIAL: {
                    mHappyVideo.start();
                    break;
                }
                case CONTROL_STATE_OPERATE: {
                    mHideOperateViewTask.remove();
                    if (mControllerView.isShowOperateView()) {
                        mControllerView.hideOperateView();
                    } else {
                        mControllerView.showOperateView();
                        if (mHappyVideo.isExecuteStart()) {
                            mHideOperateViewTask.post();
                        }
                    }
                    break;
                }
            }
        }

        private void handleStartIconClick() {
            mHideOperateViewTask.remove();
            if (mHappyVideo.isExecuteStart()) {
                mHappyVideo.pause();
                mControllerView.setStartIconStartStyle();
            } else {
                mHappyVideo.start();
                mControllerView.setStartIconPauseStyle();
                mHideOperateViewTask.post();
            }
        }

        private void handleMobileDataConfirmClick() {
            sAllowPlayWhenMobileData = true;
            switch (mCurInterceptCommand) {
                case INTERCEPT_PREPARE: {
                    mHappyVideo.prepare(mInterceptPrepareAutoStart);
                    break;
                }
                case INTERCEPT_START: {
                    mHappyVideo.start();
                    break;
                }
                case INTERCEPT_RESTART: {
                    mHappyVideo.restart();
                    break;
                }
                case INTERCEPT_RETRY: {
                    mHappyVideo.retry();
                    break;
                }
            }
        }
    }

    private class HideOperateViewTask implements Runnable {

        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void run() {
            mControllerView.hideOperateView();
        }

        void post() {
            mHandler.postDelayed(this, 3000);
        }

        void remove() {
            mHandler.removeCallbacks(this);
        }
    }
}
