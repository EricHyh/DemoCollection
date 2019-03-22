package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import java.lang.reflect.Field;

/**
 * @author Administrator
 * @description
 * @data 2019/3/1
 */

public class VideoUtils {

    private static final String TAG = "HappyVideo";
    private static final Handler sUiHandler = new Handler(Looper.getMainLooper());

    public static void log(String content) {
        Log.d(TAG, content);
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dpValue * scale);
    }

    @SuppressLint("MissingPermission")
    public static boolean isWifiEnv(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = null;
            if (manager != null) {
                info = manager.getActiveNetworkInfo();
            }
            return info != null && info.isAvailable() && info.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean isNetEnv(Context context) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = null;
            if (manager != null) {
                info = manager.getActiveNetworkInfo();
            }
            return (info != null && info.isAvailable());
        } catch (Exception e) {
            return false;
        }
    }

    public static void setProgressBarOnlyIndeterminate(ProgressBar progressBar, boolean onlyIndeterminate) {
        try {
            Field mOnlyIndeterminateField = ProgressBar.class.getDeclaredField("mOnlyIndeterminate");
            mOnlyIndeterminateField.setAccessible(true);
            mOnlyIndeterminateField.set(progressBar, onlyIndeterminate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postUiThread(Runnable runnable) {
        sUiHandler.post(runnable);
    }

    public static void postUiThreadDelayed(Runnable runnable, long delayMillis) {
        sUiHandler.postDelayed(runnable, delayMillis);
    }

    public static void removeUiThreadRunnable(Runnable runnable) {
        sUiHandler.removeCallbacks(runnable);
    }
}
