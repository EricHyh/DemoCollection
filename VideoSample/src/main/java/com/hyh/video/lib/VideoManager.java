package com.hyh.video.lib;

/**
 * Created by Eric_He on 2019/3/10.
 */

public class VideoManager {

    private final Object mLock = new Object();

    private VideoDelegate mCurrentStartVideo;

    public void onStart(VideoDelegate video) {
        synchronized (mLock) {
            if (mCurrentStartVideo == video) {
                return;
            }
            if (mCurrentStartVideo != null) {
                mCurrentStartVideo.pause();
            }
            mCurrentStartVideo = video;
        }
    }

    public void onEnd(VideoDelegate video) {
        synchronized (mLock) {
            if (mCurrentStartVideo == video) {
                mCurrentStartVideo = null;
            }
        }
    }
}