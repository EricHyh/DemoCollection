package com.hyh.video.lib;

import android.content.Context;
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
    private static final int VIEW_STATE_CONTROLLER = 1;

    private static boolean sAllowPlayWhenMobileData;

    private final MediaEventListener mControllerMediaEventListener = new ControllerMediaEventListener();
    private final MediaProgressListener mControllerMediaProgressListener = new ControllerMediaProgressListener();
    private final Context mContext;
    private final IControllerView mControllerView;
    private HappyVideo mHappyVideo;

    private int mCurInterceptCommand = INTERCEPT_NONE;
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
    public void setUp(HappyVideo happyVideo, CharSequence title) {
        this.mHappyVideo = happyVideo;
        mHappyVideo.addMediaEventListener(mControllerMediaEventListener);
        mHappyVideo.addMediaProgressListener(mControllerMediaProgressListener);

        mControllerView.setTitle(title);
        mControllerView.setInitialViewClickListener(new ControllerClickListener(ControllerClickListener.FLAG_CONTROLLER_VIEW));
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
            mControllerView.showMobileDataConfirm();
            return true;
        }
        return false;
    }

    @Override
    public void onSurfaceCreate() {
    }

    @Override
    public void onSurfaceDestroyed() {
    }

    private class ControllerMediaEventListener extends SimpleMediaEventListener {

        @Override
        public void onPreparing() {
            mControllerView.showLoadingView();
        }

        @Override
        public void onPrepared(int duration) {
            mControllerView.setDuration(duration);
        }

        @Override
        public void onStart(int currentPosition, int duration) {
        }

        @Override
        public void onPlaying(int currentPosition, int duration) {
            mControllerView.hideLoadingView();
        }

        @Override
        public void onPause(int currentPosition, int duration) {
        }

        @Override
        public void onStop(int currentPosition, int duration) {
            mControllerView.setMediaProgress(0);
            mControllerView.setCurrentPosition(0);
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
        public void onSeekStart(int seekMilliSeconds, int seekProgress) {
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
            mControllerView.hideControllerView();
        }

        @Override
        public void onCompletion() {
            mControllerView.showEndView();
        }
    }

    private class ControllerMediaProgressListener implements MediaProgressListener {

        @Override
        public void onMediaProgress(int progress, int currentPosition) {
            mControllerView.setMediaProgress(progress);
            mControllerView.setCurrentPosition(currentPosition);
        }
    }


    private class ControllerClickListener implements View.OnClickListener {

        private static final int FLAG_INITIAL_VIEW = 1;
        private static final int FLAG_CONTROLLER_VIEW = 2;
        private static final int FLAG_START_ICON = 3;
        private static final int FLAG_REPLAY_ICON = 4;
        private static final int FLAG_RETRY_ICON = 5;
        private static final int FLAG_FULLSCREEN_TOGGLE = 6;
        private static final int FLAG_MOBILE_DATA_CONFIRM = 7;
        private static final int FLAG_BACK_ICON = 8;

        private final int flag;

        ControllerClickListener(int flag) {
            this.flag = flag;
        }

        @Override
        public void onClick(View v) {
            switch (flag) {
                case FLAG_INITIAL_VIEW: {
                    mHappyVideo.start();
                    break;
                }
                case FLAG_CONTROLLER_VIEW: {

                    break;
                }
                case FLAG_START_ICON: {

                    break;
                }
                case FLAG_REPLAY_ICON: {
                    break;
                }
                case FLAG_RETRY_ICON: {
                    break;
                }
                case FLAG_FULLSCREEN_TOGGLE: {
                    break;
                }
                case FLAG_MOBILE_DATA_CONFIRM: {
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
                    break;
                }
                case FLAG_BACK_ICON: {
                    break;
                }
            }
        }
    }
}
