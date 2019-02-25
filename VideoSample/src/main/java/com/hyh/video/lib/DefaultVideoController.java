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

    }

    @Override
    public void onMediaProgress(int progress, long currentPosition, long duration) {

    }

    @Override
    public void onPreparing() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onStart(long currentPosition, long duration) {

    }

    @Override
    public void onPlaying(long currentPosition, long duration) {

    }

    @Override
    public void onPause(long currentPosition, long duration) {

    }

    @Override
    public void onStop(long currentPosition, long duration) {

    }

    @Override
    public void onBufferingStart() {
    }

    @Override
    public void onBufferingEnd() {
    }

    @Override
    public void onBufferingUpdate(int progress) {

    }

    @Override
    public void onSeekStart(int seekMilliSeconds, long currentPosition, long duration) {

    }

    @Override
    public void onSeekEnd(long currentPosition, long duration) {

    }

    @Override
    public void onError(int what, int extra) {
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onRelease(long currentPosition, long duration) {

    }
}
