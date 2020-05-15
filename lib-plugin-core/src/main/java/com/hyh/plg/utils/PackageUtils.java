package com.hyh.plg.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

/**
 * Created by tangdongwei on 2018/9/17.
 */
public class PackageUtils {

    public static PackageInfo getBlockPackageInfo(Context context, String blockPath) {
        if (TextUtils.isEmpty(blockPath)) {
            return null;
        }
        try {
            return context.getPackageManager().getPackageArchiveInfo(blockPath, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            Logger.w("get package archive info failed, block is " + blockPath);
        }
        return null;
    }


    public static boolean isActivityExist(Context context, String fullPathName) {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = packageInfo.activities;
            if (activities == null || activities.length == 0) {
                return false;
            }
            for (ActivityInfo activity : activities) {
                if (TextUtils.equals(activity.name, fullPathName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Logger.e("IN Utils, isActivityExist(), ERROR : " + e.toString());
            return false;
        }
    }

    public static int getTargetSdkVersion(Context context) {
        int targetSdkVersion = Build.VERSION.SDK_INT;
        try {
            String packageName = context.getPackageName();
            Logger.d("getTargetSdkVersion: packageName = " + packageName);
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            targetSdkVersion = applicationInfo.targetSdkVersion;
        } catch (Exception e) {
            Logger.d("get target sdk version failed: ", e);
        }
        Logger.d("getTargetSdkVersion: version = " + targetSdkVersion);
        return targetSdkVersion;
    }
}