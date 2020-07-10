package com.hyh.common.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.hyh.common.log.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2019/1/8
 */

public class IntentUtil {

    public static boolean isSystemInstallIntent(Intent intent) {
        try {
            if (intent != null) {
                String type = intent.getType();
                String dataString = intent.getDataString();
                Logger.d("IN isSysInstallerIntent : intent is " + intent);
                Logger.v("IN isSysInstallerIntent : type is : " + type + " , dataString is : " + dataString);
                return !TextUtils.isEmpty(type)
                        && type.equals("application/vnd.android.package-archive")
                        && !TextUtils.isEmpty(dataString);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSelfComponent(Context context, Intent intent) {
        if (intent == null) {
            return false;
        }
        String packageName = intent.getPackage();
        if (TextUtils.equals(packageName, context.getPackageName())) {
            return true;
        }
        ComponentName component = intent.getComponent();
        return component != null && TextUtils.equals(component.getPackageName(), context.getPackageName());
    }

    public static ResolveInfo queryIntentActivity(Context context, String deepLink) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return queryIntentActivity(context, intent);
    }

    public static ResolveInfo queryIntentActivity(Context context, Intent intent) {
        if (intent == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (activityInfo != null) {
                    String packageName = activityInfo.packageName;
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.equals("android", packageName)) {
                        return resolveInfo;
                    }
                }
            }
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
            if (resolveInfoList == null || resolveInfoList.isEmpty()) {
                return null;
            }
            return resolveInfoList.get(0);
        } catch (Exception e) {
            return null;
        }
    }


    public static List<ResolveInfo> queryIntentActivities(Context context, Intent intent) {
        if (intent == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (activityInfo != null) {
                    String packageName = activityInfo.packageName;
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.equals("android", packageName)) {
                        return Collections.singletonList(resolveInfo);
                    }
                }
            }
            return packageManager.queryIntentActivities(intent, 0);
        } catch (Exception e) {
            return null;
        }
    }


    public static boolean isDeepLinkIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        String dataString = intent.getDataString();
        return !TextUtils.isEmpty(dataString);
    }

    public static boolean isTelIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        String dataString = intent.getDataString();
        return !TextUtils.isEmpty(dataString) && dataString.startsWith("tel:");
    }

    public static boolean isRequestInstallPermissionIntent(Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false;
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        return !TextUtils.isEmpty(action) && action.equals(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
    }

    /*Uri uri = Uri.parse("market://details?id=" + packageName)*/
    /*intent.addCategory("android.intent.category.APP_MARKET")*/
    public static boolean isRequestAppStore(Intent intent) {
        if (intent == null) {
            return false;
        }
        boolean isRequestAppMarket = false;
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            isRequestAppMarket = categories.contains("android.intent.category.APP_MARKET");
        }

        if (isRequestAppMarket) {
            return true;
        }

        String dataString = intent.getDataString();

        if (!TextUtils.isEmpty(dataString) && dataString.contains("market://details")) {
            isRequestAppMarket = true;
        }

        return isRequestAppMarket;
    }


    public static String getAppFilePathFromInstallIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        String appFilePath = null;
        String dataString = intent.getDataString();
        if (!TextUtils.isEmpty(dataString) && dataString.startsWith("file://")) {
            appFilePath = dataString.substring(7, dataString.length());
        }
        return appFilePath;
    }

    public static void selectFirstActivityIfNecessary(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        if (!TextUtils.isEmpty(intent.getPackage())) {
            return;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                Logger.d("selectFirstActivityIfNecessary: default resolveInfo = " + resolveInfo);
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (activityInfo != null) {
                    String packageName = activityInfo.packageName;
                    Logger.d("selectFirstActivityIfNecessary: default packageName = " + packageName);
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.equals("android", packageName)) {
                        intent.setPackage(packageName);
                        return;
                    }
                }
            }
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
            if (resolveInfoList == null || resolveInfoList.size() <= 0) {
                Logger.d("selectFirstActivityIfNecessary: resolveInfoList size = " + 0);
                return;
            }
            Logger.d("selectFirstActivityIfNecessary: resolveInfoList size = " + resolveInfoList.size());
            resolveInfo = resolveInfoList.get(0);
            String packageName = resolveInfo.activityInfo.packageName;
            if (!TextUtils.isEmpty(packageName)) {
                intent.setPackage(packageName);
            }
        } catch (Exception e) {
            //
        }
    }

    public static ResolveInfo queryIntentService(Context context, Intent intent) {
        if (intent == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            ResolveInfo resolveInfo = packageManager.resolveService(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (activityInfo != null) {
                    String packageName = activityInfo.packageName;
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.equals("android", packageName)) {
                        return resolveInfo;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isBackHomeIntent(Intent intent) {
        if (intent == null) return false;
        String action = intent.getAction();
        if (!TextUtils.equals(action, Intent.ACTION_MAIN)) return false;
        Set<String> categories = intent.getCategories();
        if (categories == null || categories.size() != 1) return false;
        return categories.contains(Intent.CATEGORY_HOME);
    }
}