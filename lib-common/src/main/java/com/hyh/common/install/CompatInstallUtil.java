package com.hyh.common.install;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.hyh.common.log.Logger;
import com.hyh.common.utils.PackageUtil;

import java.io.File;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/4/2
 */
public class CompatInstallUtil {

    public static Intent getCompatInstallIntent(Context context, File apkFile) {
        String cmd = "chmod 777 " + apkFile.getAbsolutePath();
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Intent intent;
        if (Build.VERSION.SDK_INT >= 26) {
            int targetSdkVersion = PackageUtil.getTargetSdkVersion(context);
            if (targetSdkVersion >= 26) {
                intent = getInstallIntentApi26(context, apkFile);
            } else if (targetSdkVersion >= 24 || Build.VERSION.SDK_INT >= 29) {
                intent = getInstallIntentApi24(context, apkFile);
            } else {
                intent = getInstallIntent(context, apkFile);
            }
        } else if (Build.VERSION.SDK_INT >= 24) {
            int targetSdkVersion = PackageUtil.getTargetSdkVersion(context);
            if (targetSdkVersion >= 24) {
                intent = getInstallIntentApi24(context, apkFile);
            } else {
                intent = getInstallIntent(context, apkFile);
            }
        } else {
            intent = getInstallIntent(context, apkFile);
        }
        return intent;
    }

    private static Intent getInstallIntent(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        selectedSystemInstallerPackage(context, intent);
        return intent;
    }

    private static Intent getInstallIntentApi24(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri contentUri = getContentUriFromFile(apkFile);
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        selectedSystemInstallerPackage(context, intent);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static Intent getInstallIntentApi26(Context context, File apkFile) {
        boolean canRequestPackageInstalls = context.getPackageManager().canRequestPackageInstalls();
        if (canRequestPackageInstalls) {
            return getInstallIntentApi24(context, apkFile);
        } else {
            // TODO: 2020/7/15
            return null;
        }
    }


    public static void compatInstall(Context context, File apkFile) {
        String cmd = "chmod 777 " + apkFile.getAbsolutePath();
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 26) {
            int targetSdkVersion = PackageUtil.getTargetSdkVersion(context);
            if (targetSdkVersion >= 26) {
                installApi26(context, apkFile);
            } else if (targetSdkVersion >= 24 || Build.VERSION.SDK_INT >= 29) {
                installApi24(context, apkFile);
            } else {
                install(context, apkFile);
            }
        } else if (Build.VERSION.SDK_INT >= 24) {
            int targetSdkVersion = PackageUtil.getTargetSdkVersion(context);
            if (targetSdkVersion >= 24) {
                installApi24(context, apkFile);
            } else {
                install(context, apkFile);
            }
        } else {
            install(context, apkFile);
        }
    }

    public static void install(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        selectedSystemInstallerPackage(context, intent);
        context.startActivity(intent);
    }

    public static void installApi24(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Logger.d("installApi24 apkFile = " + apkFile.getAbsolutePath());
        Uri contentUri = getContentUriFromFile(apkFile);
        Logger.d("installApi24 contentUri = " + contentUri);
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        selectedSystemInstallerPackage(context, intent);
        context.startActivity(intent);
    }

    private static Uri getContentUriFromFile(File apkFile) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void installApi26(Context context, File apkFile) {
        boolean canRequestPackageInstalls = context.getPackageManager().canRequestPackageInstalls();
        if (canRequestPackageInstalls) {
            installApi24(context, apkFile);
        } else {
            // TODO: 2020/7/15
        }
    }

    public static boolean isNeedRequestPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            int targetSdkVersion = PackageUtil.getTargetSdkVersion(context);
            if (targetSdkVersion >= 26) {
                boolean canRequestPackageInstalls = context.getPackageManager().canRequestPackageInstalls();
                return !canRequestPackageInstalls;
            }
        }
        return false;
    }


    /**
     * 为安装应用的Intent选择一个系统安装器的包名。为什么这么干，因为在乐视手机上如果有多个安装器，会弹出安装器选择界面，FK乐视
     */
    private static void selectedSystemInstallerPackage(Context context, Intent intent) {
        if (intent == null) return;
        String intentPackage = intent.getPackage();
        if (intentPackage != null) {
            Logger.d("selectedSystemInstallerPackage intentPackage is not null, intentPackage = " + intentPackage);
            return;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
            if (list.isEmpty()) {
                Logger.d("selectedSystemInstallerPackage ResolveInfoList is empty");
                return;
            }
            String androidInstallerPackage = null;
            String installerPackage = null;
            String firstInstallerPackage = null;
            for (ResolveInfo resolveInfo : list) {
                if (resolveInfo == null) continue;
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (activityInfo == null) continue;
                ApplicationInfo applicationInfo = activityInfo.applicationInfo;
                if (applicationInfo == null || (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) continue;
                String packageName = applicationInfo.packageName;
                if (TextUtils.isEmpty(packageName)) continue;
                if (firstInstallerPackage == null) firstInstallerPackage = packageName;
                if (TextUtils.equals(packageName, "com.android.packageinstaller")) {
                    androidInstallerPackage = packageName;
                    break;
                }
                if (packageName.toLowerCase().contains("install")) {
                    installerPackage = packageName;
                }
            }
            String packageName;
            if (androidInstallerPackage != null) {
                packageName = androidInstallerPackage;
            } else if (installerPackage != null) {
                packageName = installerPackage;
            } else {
                packageName = firstInstallerPackage;
            }
            if (packageName != null) {
                intent.setPackage(packageName);
            }
            Logger.d("selectedSystemInstallerPackage packageName = " + packageName);
        } catch (Exception e) {
            Logger.w("selectedSystemInstallerPackage failed ", e);
        }
    }
}