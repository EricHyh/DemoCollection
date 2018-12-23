package com.hyh.download.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/7/13
 */

public class PackageUtil {

    public static int getVersionCode(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isServiceRunning(Context context, String serviceClassName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (TextUtils.equals(serviceClassName, service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param context
     * @return true/false表示用户是否在应用中
     */
    public static boolean isAppExit(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = null;
        if (activityManager != null) {
            appProcesses = activityManager.getRunningAppProcesses();
        }
        if (appProcesses == null || appProcesses.size() == 0)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY
                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAppInstall(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return packageInfo != null;
        } catch (Exception e) {
            return false;
        }
    }


    public static PackageInfo getPackageArchiveInfo(Context context, String filePath) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static boolean isAppFile(Context context, String filePath) {
        return !TextUtils.isEmpty(filePath) && getPackageArchiveInfo(context, filePath) != null;
    }
}
