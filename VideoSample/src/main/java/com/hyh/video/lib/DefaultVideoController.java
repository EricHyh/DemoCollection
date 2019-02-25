package com.hyh.video.lib;

import android.content.Context;
import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2019/2/23
 */
public class DefaultVideoController implements IVideoController {

    private final IControllerView mControllerView;
    private IMediaPlayer mMediaPlayer;

    public DefaultVideoController(Context context) {
        this.mControllerView = new DefaultControllerView(context);
    }

    @Override
    public View getView() {
        return mControllerView.getView();
    }

    @Override
    public void setUp(IMediaPlayer mediaPlayer) {
        this.mMediaPlayer = mediaPlayer;

    }

    @Override
    public void setTitle(CharSequence text) {
        mControllerView.setTitle(text);
    }

    @Override
    public void onMediaProgress(int progress, int currentPosition) {
        mControllerView.setMediaProgress(progress);
        mControllerView.setCurrentPosition(currentPosition);
    }

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
    public void onVideoSizeChanged(int width, int height) {
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onRelease(int currentPosition, int duration) {

    }
}
