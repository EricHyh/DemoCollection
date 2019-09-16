package com.hyh.video.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
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

    public static int[] getScreenSize(Context context) {
        int[] size = new int[2];
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Point outSize = new Point();
                windowManager.getDefaultDisplay().getRealSize(outSize);
                size[0] = outSize.x;
                size[1] = outSize.y;
            } else {
                DisplayMetrics outMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(outMetrics);
                size[0] = outMetrics.widthPixels;
                size[1] = outMetrics.heightPixels;
            }
        } else {
            Resources resources = context.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            size[0] = dm.widthPixels;
            size[1] = dm.heightPixels;
        }
        return size;
    }


    public static int getScreenOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            if (display != null) {
                int rotation = display.getRotation();
                switch (rotation) {
                    case Surface.ROTATION_0: {
                        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    }
                    case Surface.ROTATION_90: {
                        return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    }
                    case Surface.ROTATION_180: {
                        return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    }
                    case Surface.ROTATION_270: {
                        return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    }
                }
            }
        }
        return context.getResources().getConfiguration().orientation;
    }

    public static boolean isActivitySupportChangeOrientation(Activity activity) {
        if (activity == null) return false;
        try {
            ComponentName componentName = activity.getComponentName();
            if (componentName == null) {
                componentName = new ComponentName(activity, activity.getClass());
            }
            ActivityInfo activityInfo = activity.getPackageManager().getActivityInfo(componentName, 0);
            int configChanges = activityInfo.configChanges;
            boolean hasOrientationConfig = (configChanges & ActivityInfo.CONFIG_ORIENTATION) != 0;
            boolean hasScreenSizeConfig = (configChanges & ActivityInfo.CONFIG_SCREEN_SIZE) != 0;
            return hasOrientationConfig && hasScreenSizeConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAccelerometerRotationOpened(Context context) {
        try {
            int status = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            return status == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isViewInScreen(View view) {
        if (view == null
                || view.getVisibility() != View.VISIBLE
                || view.getWindowToken() == null) {
            return false;
        }
        if (!view.getLocalVisibleRect(new Rect())) {
            return false;
        }
        ViewParent parent = view.getParent();
        if (parent == null) {
            return view == view.getRootView();
        } else {
            return isParentAlive(parent);
        }
    }

    public static boolean isParentAlive(ViewParent parent) {
        if (parent instanceof View) {
            View parentView = (View) parent;
            if (parentView.getWindowToken() == null
                    || parentView.getVisibility() != View.VISIBLE) {
                return false;
            } else {
                ViewParent grandParent = parentView.getParent();
                if (grandParent == null) {
                    return parentView == parentView.getRootView();
                } else {
                    return isParentAlive(grandParent);
                }
            }
        } else {
            return true;
        }
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c;
        Object obj;
        Field field;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
}