package com.hyh.video.lib;

/**
 * Created by Eric_He on 2019/3/10.
 */

public class VideoManager {

    private final Object mLock = new Object();

    private HappyVideo mCurrentStartVideo;

    public void onStart(HappyVideo video) {
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

    public void onEnd(HappyVideo video) {
        synchronized (mLock) {
            if (mCurrentStartVideo == video) {
                mCurrentStartVideo = null;
            }
        }
    }
}