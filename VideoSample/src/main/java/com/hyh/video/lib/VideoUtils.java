package com.hyh.video.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import java.lang.reflect.Field;

/**
 * @author Administrator
 * @description
 * @data 2019/3/1
 */

public class VideoUtils {

    private static final Handler sUiHandler = new Handler(Looper.getMainLooper());

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dpValue * scale);
    }

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
            Field mOnlyIndeterminateField = progressBar.getClass().getDeclaredField("mOnlyIndeterminate");
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
