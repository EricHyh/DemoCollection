package com.hyh.video.lib;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;


/**
 * @author Administrator
 * @description
 * @data 2019/1/28
 */

public class HappyVideo extends FrameLayout {

    private VideoDelegate mVideoDelegate;

    public HappyVideo(Context context) {
        this(context, null);
    }

    public HappyVideo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HappyVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, null);
    }

    public HappyVideo(Context context, AttributeSet attrs, int defStyleAttr, VideoDelegate videoDelegate) {
        super(context, attrs, defStyleAttr);
        if (videoDelegate == null) {
            mVideoDelegate = newVideoDelegate(context);
        } else {
            mVideoDelegate = videoDelegate;
        }
        mVideoDelegate.attachedToContainer(this);
    }

    protected VideoDelegate newVideoDelegate(Context context) {
        return new VideoDelegate(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mVideoDelegate.onAttachedToWindow(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVideoDelegate.onDetachedFromWindow(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        mVideoDelegate.onWindowFocusChanged(this, hasWindowFocus);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && getScene() == VideoDelegate.Scene.FULLSCREEN) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                mVideoDelegate.onBackPress();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void followViewAttachState(View view) {
        mVideoDelegate.followViewAttachState(view);
    }

    public void unfollowViewAttachState(View view) {
        mVideoDelegate.unfollowViewAttachState(view);
    }

    public void setSurfaceMeasurer(ISurfaceMeasurer surfaceMeasurer) {
        if (surfaceMeasurer == null) throw new NullPointerException("HappyVideo setSurfaceMeasurer can't be null");
        mVideoDelegate.setSurfaceMeasurer(surfaceMeasurer);
    }

    public void setVideoBackground(IVideoBackground background) {
        mVideoDelegate.setVideoBackground(background);
    }

    public void setVideoPreview(IVideoPreview videoPreview) {
        mVideoDelegate.setVideoPreview(videoPreview);
    }

    public void setVideoController(IVideoController controller) {
        mVideoDelegate.setVideoController(controller);
    }

    public void addMediaEventListener(MediaEventListener listener) {
        mVideoDelegate.addMediaEventListener(listener);
    }

    public void removeMediaEventListener(MediaEventListener listener) {
        mVideoDelegate.removeMediaEventListener(listener);
    }

    public void addMediaProgressListener(MediaProgressListener listener) {
        mVideoDelegate.addMediaProgressListener(listener);
    }

    public void removeMediaProgressListener(MediaProgressListener listener) {
        mVideoDelegate.removeMediaProgressListener(listener);
    }

    public void addSurfaceListener(IVideoSurface.SurfaceListener listener) {
        mVideoDelegate.addSurfaceListener(listener);
    }

    public void removeSurfaceListener(IVideoSurface.SurfaceListener listener) {
        mVideoDelegate.removeSurfaceListener(listener);
    }

    public boolean setup(DataSource source, CharSequence title, boolean looping) {
        return mVideoDelegate.setup(source, title, looping);
    }

    public void setFullscreenActivity(Activity activity) {
        mVideoDelegate.setFullscreenActivity(activity);
    }

    public void setFullscreenAllowLandscape(boolean allowLandscape) {
        mVideoDelegate.setFullscreenAllowLandscape(allowLandscape);
    }

    public void setFullscreenAllowRotate(boolean allowRotate) {
        mVideoDelegate.setFullscreenAllowRotate(allowRotate);
    }

    public int getScene() {
        return mVideoDelegate.getScene();
    }

    public boolean startFullscreenScene() {
        return mVideoDelegate.startFullscreenScene();
    }

    public boolean recoverNormalScene() {
        return mVideoDelegate.recoverNormalScene();
    }

    public int getMediaState() {
        return mVideoDelegate.getMediaState();
    }

    public DataSource getDataSource() {
        return mVideoDelegate.getDataSource();
    }

    public void setLooping(boolean looping) {
        mVideoDelegate.setLooping(looping);
    }

    public boolean isLooping() {
        return mVideoDelegate.isLooping();
    }

    public void prepare(boolean autoStart) {
        mVideoDelegate.prepare(autoStart);
    }

    public void start() {
        mVideoDelegate.start();
    }

    public void restart() {
        mVideoDelegate.restart();
    }

    public void retry() {
        mVideoDelegate.retry();
    }

    public void pause() {
        mVideoDelegate.pause();
    }

    public void stop() {
        mVideoDelegate.stop();
    }

    public void release() {
        mVideoDelegate.release();
    }

    public boolean isExecuteStart() {
        return mVideoDelegate.isExecuteStart();
    }

    public boolean isPlaying() {
        return mVideoDelegate.isPlaying();
    }

    public boolean isReleased() {
        return mVideoDelegate.isReleased();
    }

    public void seekTimeTo(int milliSeconds) {
        mVideoDelegate.seekTimeTo(milliSeconds);
    }

    public void seekProgressTo(int progress) {
        mVideoDelegate.seekProgressTo(progress);
    }

    public long getCurrentPosition() {
        return mVideoDelegate.getCurrentPosition();
    }

    public long getDuration() {
        return mVideoDelegate.getDuration();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mVideoDelegate.setVolume(leftVolume, rightVolume);
    }

    public boolean isSupportSpeed() {
        return mVideoDelegate.isSupportSpeed();
    }

    public void setSpeed(float speed) {
        mVideoDelegate.setSpeed(speed);
    }

}