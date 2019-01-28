package com.yly.mob.ssp.video;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 这个类用来和jzvd互相调用，当jzvd需要调用Media的时候调用这个类，当MediaPlayer有回调的时候，通过这个类回调JZVD
 * Created by Nathen on 2017/11/18.
 */
public class JZMediaManager {

    private static final String TAG = "JZVD";

    private static JZMediaManager jzMediaManager;

    private JzvdRuntime mJzvdRuntime;

    public JZMediaInterface jzMediaInterface;

    public HandlerThread mMediaHandlerThread;
    public Handler mainThreadHandler;

    private int mVideoWidth;
    private int mVideoHeight;


    private JZMediaManager() {
        mMediaHandlerThread = new HandlerThread(TAG);
        mMediaHandlerThread.start();
        mainThreadHandler = new Handler();
        if (jzMediaInterface == null) {
            jzMediaInterface = new JZMediaSystem();
        }
    }

    public static JZMediaManager instance() {
        if (jzMediaManager == null) {
            jzMediaManager = new JZMediaManager();
        }
        return jzMediaManager;
    }

    public JzvdRuntime createJZTextureView(Jzvd jzvd, JZDataSource jzDataSource) {
        jzMediaInterface.jzDataSource = jzDataSource;
        JZTextureView textureView = new JZTextureView(jzvd.getApplicationContext());
        textureView.setRotation(jzvd.mVideoRotation);
        textureView.setScaleType(jzvd.mScaleType);
        mJzvdRuntime = new JzvdRuntime(jzMediaInterface, textureView);
        return mJzvdRuntime;
    }

    public JzvdRuntime getCurrentJzvdRuntime() {
        return mJzvdRuntime;
    }

    public void prepare() {
        if (mJzvdRuntime != null) {
            mJzvdRuntime.prepare();
        }
    }

    public void releaseCurrentJzvd() {
        if (mJzvdRuntime != null) {
            mJzvdRuntime.release();
            mJzvdRuntime = null;
        }
    }

    public void releaseMediaPlayer() {
        if (mJzvdRuntime != null) {
            mJzvdRuntime.releaseMediaPlayer();
        }
    }


    //这几个方法是不是多余了，为了不让其他地方动MediaInterface的方法
    public static void setDataSource(JZDataSource jzDataSource) {
        instance().jzMediaInterface.jzDataSource = jzDataSource;
    }

    public static JZDataSource getDataSource() {
        return instance().jzMediaInterface.jzDataSource;
    }

    //正在播放的url或者uri
    public static Object getCurrentUrl() {
        return instance().jzMediaInterface.jzDataSource == null ? null : instance().jzMediaInterface.jzDataSource.getCurrentUrl();
    }

    public static long getCurrentPosition() {
        return instance().jzMediaInterface.getCurrentPosition();
    }

    public static long getDuration() {
        return instance().jzMediaInterface.getDuration();
    }

    public static void seekTo(long time) {
        instance().jzMediaInterface.seekTo(time);
    }

    public static void pause() {
        instance().jzMediaInterface.pause();
    }

    public static void start() {
        instance().jzMediaInterface.start();
    }

    public static boolean isPrepared() {
        return instance().jzMediaInterface.isPrepared();
    }

    public static boolean isPlaying() {
        return instance().jzMediaInterface.isPlaying();
    }

    public static void setSpeed(float speed) {
        instance().jzMediaInterface.setSpeed(speed);
    }

    int getVideoWidth() {
        return mVideoWidth;
    }

    int getVideoHeight() {
        return mVideoHeight;
    }

    public void setVideoWidth(int videoWidth) {
        mVideoWidth = videoWidth;
    }

    public void setVideoHeight(int videoHeight) {
        mVideoHeight = videoHeight;
    }
}
