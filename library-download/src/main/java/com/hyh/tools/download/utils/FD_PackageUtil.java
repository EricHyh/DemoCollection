package com.hyh.tools.download.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/2/7
 */

public class FD_PackageUtil {

    public static String getUserAgent() {
        return System.getProperty("http.agent");
    }

    public static String getUserAgent(Context context) {
        String userAgent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            userAgent = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userAgent;
    }


    /**
     * @return 手机中所有已安装的非系统应用程序的包名列表
     */
    public static List<String> getInstalledApps(Context context) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
        List<String> packageList = new ArrayList<>();
        if (installedPackages == null || installedPackages.isEmpty()) {
            return packageList;
        }
        for (int i = 0; i < installedPackages.size(); i++) {
            String packageName = installedPackages.get(i).packageName;
            /*如果是系統应用则不处理*/
            if (installedPackages.get(i).applicationInfo.sourceDir.contains("system/")) {
                continue;
            }
            packageList.add(packageName);
        }
        return packageList;
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
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                return true;
            } else {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int getVersionCode(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (TextUtils.equals(packageName, packageInfo.packageName)) {
                return packageInfo.versionCode;
            }
        }
        return -1;
    }
}
