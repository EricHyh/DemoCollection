package com.hyh.common.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.WindowManager;

import com.hyh.common.receiver.ScreenListener;
import com.hyh.common.receiver.ScreenReceiver;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2019/9/9
 */

public class KeepScreenOnHelper implements ScreenListener {

    private Context mContext;
    private WeakReference<Activity> mActivityRef;
    private KeepScreenOnHandler mKeepScreenOnHandler = new KeepScreenOnHandler();

    public void onActivityCreate(Activity activity) {
        this.mContext = activity.getApplicationContext();
        this.mActivityRef = new WeakReference<>(activity);
        ScreenReceiver.getInstance(mContext).addListener(this);

        onUserInteraction();
    }

    public void onUserInteraction() {
        if (shouldKeepScreenOn()) {
            if (isKeepScreenOn()) {
                mKeepScreenOnHandler.restartCancelKeepScreenOnTask();
            } else {
                mKeepScreenOnHandler.startKeepScreenOnTask();
            }
        }
    }

    public void onActivityDestroy() {
        ScreenReceiver.getInstance(mContext).removeListener(this);
        mKeepScreenOnHandler.removeTask();
    }

    @Override
    public void onScreenOn(Context context, BroadcastReceiver receiver, Intent intent) {
        onUserInteraction();
    }

    @Override
    public void onScreenOff(Context context, BroadcastReceiver receiver, Intent intent) {
        mKeepScreenOnHandler.removeTask();
    }

    private boolean shouldKeepScreenOn() {
        return PhoneStateUtil.inKeyguardRestrictedInputMode(mContext);
    }

    private boolean isKeepScreenOn() {
        Activity activity = mActivityRef.get();
        return activity != null && (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0;
    }

    @SuppressLint("HandlerLeak")
    private class KeepScreenOnHandler extends Handler {

        private static final int MSG_KEEP_SCREEN_ON = 100;
        private static final int MSG_CANCEL_KEEP_SCREEN_ON = 101;


        KeepScreenOnHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mActivityRef.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_KEEP_SCREEN_ON: {
                        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    }
                    case MSG_CANCEL_KEEP_SCREEN_ON: {
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    }
                }
            }
        }

        void startKeepScreenOnTask() {
            sendEmptyMessage(MSG_KEEP_SCREEN_ON);
            removeMessages(MSG_CANCEL_KEEP_SCREEN_ON);
            /*if (BuildConfig.DEBUG) {
                sendEmptyMessageDelayed(MSG_CANCEL_KEEP_SCREEN_ON, 300 * 1000);
            } else {
                sendEmptyMessageDelayed(MSG_CANCEL_KEEP_SCREEN_ON, 30 * 1000);
            }*/
            sendEmptyMessageDelayed(MSG_CANCEL_KEEP_SCREEN_ON, 30 * 1000);
        }

        void restartCancelKeepScreenOnTask() {
            removeMessages(MSG_CANCEL_KEEP_SCREEN_ON);
            /*if (BuildConfig.DEBUG) {
                sendEmptyMessageDelayed(MSG_CANCEL_KEEP_SCREEN_ON, 300 * 1000);
            } else {
                sendEmptyMessageDelayed(MSG_CANCEL_KEEP_SCREEN_ON, 30 * 1000);
            }*/
            sendEmptyMessageDelayed(MSG_CANCEL_KEEP_SCREEN_ON, 30 * 1000);
        }

        void removeTask() {
            removeMessages(MSG_KEEP_SCREEN_ON);
            removeMessages(MSG_CANCEL_KEEP_SCREEN_ON);
        }
    }
}