package com.hyh.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2018/11/5
 */

public class PackageInstallReceiver extends BroadcastReceiver {

    private static PackageInstallReceiver sPackageInstallReceiver;

    public static PackageInstallReceiver getInstance(Context context) {
        if (sPackageInstallReceiver != null) {
            sPackageInstallReceiver.tryToRegister(context.getApplicationContext());
            return sPackageInstallReceiver;
        }
        synchronized (PackageInstallReceiver.class) {
            if (sPackageInstallReceiver == null) {
                sPackageInstallReceiver = new PackageInstallReceiver(context.getApplicationContext());
            }
        }
        return sPackageInstallReceiver;
    }

    private Map<String, List<PackageInstallListener>> mPackageInstallListenersMap = new ConcurrentHashMap<>();

    private boolean isRegister;

    private PackageInstallReceiver(Context context) {
        tryToRegister(context);
    }

    private void tryToRegister(Context context) {
        if (isRegister) {
            return;
        }
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            intentFilter.addDataScheme("package");
            context.registerReceiver(this, intentFilter);
            isRegister = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(String packageName, PackageInstallListener listener) {
        if (TextUtils.isEmpty(packageName) || listener == null) {
            return;
        }
        List<PackageInstallListener> packageInstallListeners = mPackageInstallListenersMap.get(packageName);
        if (packageInstallListeners == null) {
            packageInstallListeners = new CopyOnWriteArrayList<>();
            packageInstallListeners.add(listener);
            mPackageInstallListenersMap.put(packageName, packageInstallListeners);

        } else {
            if (!packageInstallListeners.contains(listener)) {
                packageInstallListeners.add(listener);
            }
        }
    }

    public void removeListener(String packageName, PackageInstallListener listener) {
        if (TextUtils.isEmpty(packageName) || listener == null) {
            return;
        }
        List<PackageInstallListener> packageInstallListeners = mPackageInstallListenersMap.get(packageName);
        if (packageInstallListeners == null || packageInstallListeners.isEmpty()) {
            return;
        }
        packageInstallListeners.remove(listener);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (mPackageInstallListenersMap.isEmpty()) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.equals(action, Intent.ACTION_PACKAGE_ADDED) || TextUtils.equals(action, Intent.ACTION_PACKAGE_REPLACED)) {
            Uri data = intent.getData();
            if (data == null) {
                return;
            }
            String packageName = data.getSchemeSpecificPart();
            if (TextUtils.isEmpty(packageName)) {
                return;
            }
            List<PackageInstallListener> packageInstallListeners = mPackageInstallListenersMap.get(packageName);
            if (packageInstallListeners == null || packageInstallListeners.isEmpty()) {
                return;
            }
            for (PackageInstallListener packageInstallListener : packageInstallListeners) {
                if (packageInstallListener != null) {
                    packageInstallListener.onInstall(packageName);
                }
            }
        }
    }
}
