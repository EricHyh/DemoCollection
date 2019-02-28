package com.hyh.video.lib;

import android.content.Context;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */
public class DefaultVideoController implements IVideoController {


    private static boolean sIsAllowPlayWhenMobileData;

    private final MediaEventListener mControllerMediaEventListener = new ControllerMediaEventListener();
    private final MediaProgressListener mControllerMediaProgressListener = new ControllerMediaProgressListener();
    private final IControllerView mControllerView;
    private HappyVideo mHappyVideo;

    public DefaultVideoController(Context context) {
        this.mControllerView = new DefaultControllerView(context);
    }

    public DefaultVideoController(IControllerView controllerView) {
        this.mControllerView = controllerView;
    }

    @Override
    public View getView() {
        return mControllerView.getView();
    }

    @Override
    public void setUp(HappyVideo happyVideo) {
        this.mHappyVideo = happyVideo;
        mHappyVideo.addMediaEventListener(mControllerMediaEventListener);
        mHappyVideo.addMediaProgressListener(mControllerMediaProgressListener);
    }

    @Override
    public void setTitle(CharSequence text) {
        mControllerView.setTitle(text);
    }

    @Override
    public boolean interceptPrepare(boolean autoStart) {

        return false;
    }

    @Override
    public boolean interceptStart() {
        return false;
    }

    @Override
    public boolean interceptReStart() {
        return false;
    }

    @Override
    public boolean interceptRetry() {
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
            mControllerView.hideErrorView();
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
            mControllerView.showBottomProgress();
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


    private class ControllerClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

        }
    }
}
