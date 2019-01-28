package com.yly.mob.ssp.video;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * @author Administrator
 * @description
 * @data 2019/1/27
 */

class JzvdTouchHelper {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_POSITION = 1;
    public static final int TYPE_VOLUME = 2;
    public static final int TYPE_BRIGHTNESS = 3;

    public static final int THRESHOLD = 80;

    private final Jzvd mJzvd;
    private final OnJzvdFullViewGestureCallback mGestureCallback;
    private final AudioManager mAudioManager;
    private final int mScreenWidth;
    private final int mScreenHeight;

    private float mActionDownX;
    private float mActionDownY;
    private boolean mControlPosition, mControlVolume, mControlBrightness;
    private int mGestureType;

    private long mGestureStartPosition;
    private int mGestureStartVolume;
    private float mGestureStartBrightness;
    private long mSeekTimePosition;


    JzvdTouchHelper(Jzvd jzvd, OnJzvdFullViewGestureCallback callback) {
        this.mJzvd = jzvd;
        this.mGestureCallback = callback;
        this.mAudioManager = (AudioManager) mJzvd.getContext().getSystemService(Context.AUDIO_SERVICE);
        this.mScreenWidth = jzvd.getContext().getResources().getDisplayMetrics().widthPixels;
        this.mScreenHeight = jzvd.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mActionDownX = event.getX();
                mActionDownY = event.getY();
                mControlPosition = false;
                mControlVolume = false;
                mControlBrightness = false;
                mGestureType = 0;

                if (mGestureCallback != null) {
                    mGestureCallback.onActionDown();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                float deltaX = x - mActionDownX;
                float deltaY = y - mActionDownY;
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (!mControlPosition && !mControlVolume && !mControlBrightness) {
                    if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                        if (absDeltaX >= THRESHOLD) {
                            // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                            // 否则会因为mediaplayer的状态非法导致App Crash
                            if (mJzvd.currentState != Jzvd.CURRENT_STATE_ERROR) {
                                mControlPosition = true;
                                mGestureType = TYPE_POSITION;
                                mGestureStartPosition = mJzvd.getCurrentPositionWhenPlaying();
                                if (mGestureCallback != null) {
                                    mGestureCallback.onStartControl(TYPE_POSITION);
                                }
                            }
                        } else {
                            //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                            if (mActionDownX < mScreenWidth * 0.5f) {//左侧改变亮度
                                mControlBrightness = true;
                                mGestureType = TYPE_BRIGHTNESS;
                                mGestureStartBrightness = getBrightness();
                                if (mGestureCallback != null) {
                                    mGestureCallback.onStartControl(TYPE_BRIGHTNESS);
                                }
                            } else {//右侧改变声音
                                mControlVolume = true;
                                mGestureType = TYPE_VOLUME;
                                mGestureStartVolume = getVolume();
                                if (mGestureCallback != null) {
                                    mGestureCallback.onStartControl(TYPE_VOLUME);
                                }
                            }
                        }
                    }
                }
                if (mControlPosition) {
                    long totalTimeDuration = mJzvd.getDuration();
                    mSeekTimePosition = (int) (mGestureStartPosition + deltaX * totalTimeDuration / mScreenWidth);
                    if (mSeekTimePosition > totalTimeDuration) {
                        mSeekTimePosition = totalTimeDuration;
                    }
                    String seekTime = JZUtils.stringForTime(mSeekTimePosition);
                    String totalTime = JZUtils.stringForTime(totalTimeDuration);
                    if (mGestureCallback != null) {
                        mGestureCallback.onControlPosition(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    }
                }
                if (mControlVolume) {
                    deltaY = -deltaY;
                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureStartVolume + deltaV, 0);
                    //dialog中显示百分比
                    int volumePercent = (int) (mGestureStartVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                    if (mGestureCallback != null) {
                        mGestureCallback.onControlVolume(-deltaY, volumePercent);
                    }
                }
                if (mControlBrightness) {
                    deltaY = -deltaY;
                    int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                    //WindowManager.LayoutParams params = JZUtils.getWindow(getContext()).getAttributes();
                    WindowManager.LayoutParams params = mJzvd.mWindowController.getAttributes();
                    if (((mGestureStartBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                        params.screenBrightness = 1;
                    } else if (((mGestureStartBrightness + deltaV) / 255) <= 0) {
                        params.screenBrightness = 0.01f;
                    } else {
                        params.screenBrightness = (mGestureStartBrightness + deltaV) / 255;
                    }
                    mJzvd.mWindowController.setAttributes(params);
                    //dialog中显示百分比
                    int brightnessPercent = (int) (mGestureStartBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                    if (mGestureCallback != null) {
                        mGestureCallback.onControlBrightness(-deltaY, brightnessPercent);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mGestureCallback != null) {
                    mGestureCallback.onEndControl(mGestureType);
                }
                break;
            }
        }
        return false;
    }

    private float getBrightness() {
        WindowManager.LayoutParams lp = mJzvd.mWindowController.getAttributes();
        float brightness = 0.0f;
        if (lp.screenBrightness < 0) {
            try {
                brightness = Settings.System.getInt(mJzvd.getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            brightness = lp.screenBrightness * 255;
        }
        return brightness;
    }

    private int getVolume() {
        if (mAudioManager != null) {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    public interface OnJzvdFullViewGestureCallback {

        void onActionDown();

        void onStartControl(int gestureType);

        void onControlPosition(float deltaX, String seekTime, long seekTimePosition, String totalTime, long totalTimeDuration);

        void onControlVolume(float deltaY, int volumePercent);

        void onControlBrightness(float deltaY, int brightnessPercent);

        void onEndControl(int gestureType);

    }
}
