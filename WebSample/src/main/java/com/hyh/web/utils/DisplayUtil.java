package com.hyh.web.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Eric_He on 2016/10/3.
 */

public class DisplayUtil {

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int[] getScreenSize(Context context) {
        int[] size = new int[2];
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        size[0] = dm.widthPixels;
        size[1] = dm.heightPixels;
        return size;
    }

    public static int getScreenWidth(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }


    public static int getDensityDpi(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.densityDpi;
    }

    public static String getDensityDpiStr(Context context) {
        int densityDpi = getDensityDpi(context);
        String densityDpiStr = null;
        if (densityDpi <= 120) {
            densityDpiStr = "ldpi";
        } else if (densityDpi > 120 && densityDpi <= 160) {
            densityDpiStr = "mdpi";
        } else if (densityDpi > 160 && densityDpi <= 240) {
            densityDpiStr = "hdpi";
        } else if (densityDpi > 240 && densityDpi <= 320) {
            densityDpiStr = "xhdpi";
        } else if (densityDpi > 320 && densityDpi <= 480) {
            densityDpiStr = "xxhdpi";
        } else if (densityDpi > 480) {
            densityDpiStr = "xxxhdpi";
        }
        return densityDpiStr;
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

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public boolean hasNavigationBar(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        float density = dm.density;

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(realDisplayMetrics);
        } else {
            Class c;
            try {
                c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, realDisplayMetrics);
            } catch (Exception e) {
                realDisplayMetrics.setToDefaults();
                e.printStackTrace();
            }
        }

        int creenRealHeight = realDisplayMetrics.heightPixels;
        int creenRealWidth = realDisplayMetrics.widthPixels;

        float diagonalPixels = (float) Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));
        float screenSize = (diagonalPixels / (160f * density)) * 1f;

        Resources rs = activity.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        boolean hasNavBarFun = false;
        if (id > 0) {
            hasNavBarFun = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavBarFun = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavBarFun = true;
            }
        } catch (Exception e) {
            hasNavBarFun = false;
        }
        return hasNavBarFun;
    }

    public static int getScreenOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return context.getResources().getConfiguration().orientation;
        } else {
            int rotation = windowManager.getDefaultDisplay().getRotation();
            int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            switch (rotation) {
                case Surface.ROTATION_0: {
                    screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                }
                case Surface.ROTATION_90: {
                    screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                }
                case Surface.ROTATION_180: {
                    screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                }
                case Surface.ROTATION_270: {
                    screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                }
            }
            return screenOrientation;
        }
    }

    public static int[] getScreenSize(Activity activity) {
        int[] size = new int[2];
        WindowManager wm = (WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        size[0] = point.x;
        size[1] = point.y;
        return size;
    }

    public static float getScreenDensity(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.density;
    }


    public static boolean isDeviceSecureCompat(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return isDeviceSecure(context);
        } else {
            return isKeyguardSecure(context);
        }
    }


    /**
     * 判断设备是否设置了密码锁（PIN, pattern or password or a SIM card）
     *
     * @param context
     * @return
     */
    public static boolean isKeyguardSecure(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            return keyguardManager != null && keyguardManager.isKeyguardSecure();
        } else {
            return isSecure(context);
        }
    }

    private static boolean isSecure(Context context) {
        boolean isSecured;
        String classPath = "com.android.internal.widget.LockPatternUtils";
        try {
            Class<?> lockPatternClass = Class.forName(classPath);
            Object lockPatternObject = lockPatternClass.getConstructor(Context.class).newInstance(context);
            Method method = lockPatternClass.getMethod("isSecure");
            isSecured = (boolean) method.invoke(lockPatternObject);
        } catch (Exception e) {
            isSecured = false;
        }
        return isSecured;
    }

    /**
     * 判断设备是否设置了密码锁（PIN, pattern or password）
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isDeviceSecure(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager == null) {
            return false;
        }
        return keyguardManager.isDeviceSecure();
    }


    /**
     * 判断当前是否处于锁屏状态
     *
     * @param context
     * @return
     */
    public static boolean isDeviceLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return keyguardManager.isDeviceLocked();
        } else {
            return keyguardManager.isKeyguardLocked();
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean isDeviceLockedByFingerprint(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isDeviceLocked(context)) {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager == null) {
                return false;
            }
            if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断当前是否处于锁屏状态
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isKeyguardLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager != null && mKeyguardManager.isKeyguardLocked();
    }

    /**
     * 判断当前是否处于锁屏界面
     *
     * @param context
     * @return
     */
    public static boolean inKeyguardRestrictedInputMode(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
    }


    /**
     * 屏幕是否处于亮屏状态
     *
     * @param context
     * @return
     */
    public static boolean isScreenOn(Context context) {
        PowerManager powerManger = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManger == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManger.isInteractive();
        } else {
            return powerManger.isScreenOn();
        }
    }


    /**
     * 屏幕是否处于黑屏状态
     *
     * @param context
     * @return
     */
    public static boolean isScreenOff(Context context) {
        return !isScreenOn(context);
    }


    /**
     * 判断view是否在屏幕范围内
     *
     * @param view
     * @return
     */
    public static boolean isViewInScreen(View view) {
        if (view == null) {
            return false;
        }
        Rect rect = new Rect();
        return view.getLocalVisibleRect(rect);
    }

    public static float getVisibleScale(View view) {
        if (view == null) {
            return 0.0f;
        }
        int childWidth = view.getMeasuredWidth();
        int childHeight = view.getMeasuredHeight();
        if (childWidth <= 0 || childHeight <= 0) {
            return 0.0f;
        }
        ViewParent parent = view.getParent();
        if (parent == null || !(parent instanceof ViewGroup)) {
            return 0.0f;
        }
        ViewGroup viewGroup = (ViewGroup) parent;
        if (!isViewInScreen(viewGroup)) {
            return 0.0f;
        }
        int parentWidth = viewGroup.getMeasuredWidth();
        int parentHeight = viewGroup.getMeasuredHeight();
        if (parentWidth <= 0 || parentHeight <= 0) {
            return 0.0f;
        }
        float scale = 0.0f;
        int[] parentLocation = new int[2];
        viewGroup.getLocationOnScreen(parentLocation);
        int[] childLocation = new int[2];
        view.getLocationOnScreen(childLocation);
        int relativeY = childLocation[1] - parentLocation[1];
        if (relativeY <= 0) {
            relativeY = -relativeY;
            if (childHeight - relativeY > 0) {
                scale = (childHeight - relativeY) * 1.0f / childHeight;
            }
        } else if (relativeY > 0) {
            if (parentHeight - relativeY > 0) {
                scale = (parentHeight - relativeY) * 1.0f / childHeight;
            }
        }
        if (scale > 1.0f) {
            scale = 1.0f;
        }
        return scale;
    }

    public static boolean isParent(ViewGroup itemParent, View child) {
        if (itemParent == null) {
            return false;
        }
        ViewParent parent = child.getParent();
        if (parent == null) {
            return false;
        }
        if (parent == itemParent) {
            return true;
        } else if (parent instanceof View) {
            return isParent(itemParent, (View) parent);
        }
        return false;
    }
}
