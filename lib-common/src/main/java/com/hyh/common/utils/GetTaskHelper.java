package com.hyh.common.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.hyh.common.log.Logger;
import com.hyh.common.receiver.ScreenListener;
import com.hyh.common.receiver.ScreenReceiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2019/9/5
 */
@SuppressLint("HandlerLeak")
public class GetTaskHelper implements ScreenListener {

    private volatile static GetTaskHelper sInstance;

    public static GetTaskHelper getInstance(Context context) {
        if (sInstance != null) return sInstance;
        synchronized (GetTaskHelper.class) {
            if (sInstance == null) {
                sInstance = new GetTaskHelper(context);
            }
        }
        return sInstance;
    }

    private final Context mContext;

    private final TopPackageDetectHandler mTopPackageDetectHandler = new TopPackageDetectHandler();

    private Boolean mHasGetTaskPermission;

    private volatile boolean mIsEnabled;

    private List<TopActivityChangedListener> mListeners = new CopyOnWriteArrayList<>();

    private GetTaskHelper(Context context) {
        mContext = context;
        boolean contains = PreferenceUtil.contains(mContext,
                CommonConstants.Preference.ShareName.COMMON_PARAMS,
                CommonConstants.Preference.Key.HAS_GET_TASK_PERMISSION);

        if (contains) {
            mHasGetTaskPermission = PreferenceUtil.getBoolean(mContext,
                    CommonConstants.Preference.ShareName.COMMON_PARAMS,
                    CommonConstants.Preference.Key.HAS_GET_TASK_PERMISSION,
                    false);
        } else {
            checkGetTaskPermission();
        }
    }

    private void checkGetTaskPermission() {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                String[] getTaskPermissions = {Manifest.permission.GET_TASKS, "android.permission.REAL_GET_TASKS"};
                Boolean registered = PermissionUtil.isPermissionsRegistered(mContext, getTaskPermissions);
                if (registered == null) {
                    Logger.d("checkGetTaskPermission isPermissionsRegistered execute failed");
                    return;
                }
                Logger.d("checkGetTaskPermission isPermissionsRegistered = " + registered);
                if (!registered) {
                    mHasGetTaskPermission = false;
                    PreferenceUtil.putBoolean(mContext,
                            CommonConstants.Preference.ShareName.COMMON_PARAMS,
                            CommonConstants.Preference.Key.HAS_GET_TASK_PERMISSION,
                            false);
                    return;
                }
                PackageInfo packageInfo = PackageUtil.getPackageInfo(mContext);
                if (packageInfo == null) {
                    return;
                }
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    Boolean isPrivateApp = isPrivateApp(mContext);
                    if (isPrivateApp != null && isPrivateApp) {
                        mHasGetTaskPermission = true;
                        PreferenceUtil.putBoolean(mContext,
                                CommonConstants.Preference.ShareName.COMMON_PARAMS,
                                CommonConstants.Preference.Key.HAS_GET_TASK_PERMISSION,
                                true);
                        return;
                    }
                    Boolean isSystemApp = isSystemApp(mContext);
                    if (isSystemApp != null) {
                        mHasGetTaskPermission = isSystemApp;
                        PreferenceUtil.putBoolean(mContext,
                                CommonConstants.Preference.ShareName.COMMON_PARAMS,
                                CommonConstants.Preference.Key.HAS_GET_TASK_PERMISSION,
                                isSystemApp);
                    }
                } else {
                    Boolean isPrivateApp = isPrivateApp(mContext);
                    if (isPrivateApp != null) {
                        mHasGetTaskPermission = isPrivateApp;
                        PreferenceUtil.putBoolean(mContext,
                                CommonConstants.Preference.ShareName.COMMON_PARAMS,
                                CommonConstants.Preference.Key.HAS_GET_TASK_PERMISSION,
                                isPrivateApp);
                    }
                }
            }
        });
    }

    private Boolean isPrivateApp(Context context) {
        File dir = new File("/system/priv-app");
        if (!dir.exists()) {
            return null;
        }
        List<File> apkFiles = new ArrayList<>();
        findApkFiles(dir, apkFiles);
        if (apkFiles.isEmpty()) {
            return null;
        }
        boolean isGetPackageInfoFailed = false;
        for (File apkFile : apkFiles) {
            PackageInfo packageInfo = PackageUtil.getPackageArchiveInfo(context, apkFile.getAbsolutePath());
            if (packageInfo == null) {
                isGetPackageInfoFailed = true;
                continue;
            }
            if (TextUtils.equals(packageInfo.packageName, context.getPackageName())) {
                return true;
            }
        }
        if (isGetPackageInfoFailed) {
            return null;
        }
        return false;
    }

    private void findApkFiles(File dir, List<File> apkFiles) {
        File[] files = dir.listFiles();
        if (files == null || files.length <= 0) return;
        for (File file : files) {
            if (file.isDirectory()) {
                findApkFiles(file, apkFiles);
            } else {
                if (file.getPath().endsWith(".apk")) {
                    apkFiles.add(file);
                }
            }
        }
    }

    private Boolean isSystemApp(Context context) {
        String selfSignature = PackageUtil.getAppSignatureFingerprint(context, context.getPackageName());
        if (TextUtils.isEmpty(selfSignature)) {
            return null;
        }
        String settingPackageName = PackageUtil.getSettingPackageName(context);
        if (TextUtils.isEmpty(settingPackageName)) {
            return null;
        }
        String systemSignature = PackageUtil.getAppSignatureFingerprint(context, settingPackageName);
        if (TextUtils.isEmpty(systemSignature)) {
            return null;
        }
        return TextUtils.equals(selfSignature, systemSignature);
    }

    public boolean hasGetTaskPermission() {
        return mHasGetTaskPermission == null ? false : mHasGetTaskPermission;
    }

    public void enable() {
        this.mIsEnabled = true;
        ScreenReceiver.getInstance(mContext).addListener(this);
        if (shouldStartObserve()) {
            startObserve();
        }
    }

    public void disable() {
        this.mIsEnabled = false;
        ScreenReceiver.getInstance(mContext).removeListener(this);
    }

    @Override
    public void onScreenOn(Context context, BroadcastReceiver receiver, Intent intent) {
        if (shouldStartObserve()) {
            startObserve();
        }
    }

    @Override
    public void onScreenOff(Context context, BroadcastReceiver receiver, Intent intent) {
    }

    public void addListener(TopActivityChangedListener listener) {
        if (listener == null) return;
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
        if (shouldStartObserve()) {
            startObserve();
        }
    }

    public void removeListener(TopActivityChangedListener listener) {
        if (listener == null) return;
        mListeners.remove(listener);
    }


    private void startObserve() {
        mTopPackageDetectHandler.sendEmptyMessage(TopPackageDetectHandler.MSG_ID);
    }

    private boolean shouldStartObserve() {
        return mIsEnabled && !mListeners.isEmpty() && ScreenReceiver.getInstance(mContext).isScreenOn();
    }

    private class TopPackageDetectHandler extends Handler {

        static final int MSG_ID = 1;

        private ComponentName mOldComponentName;

        TopPackageDetectHandler() {
            super(ThreadUtil.getBackThreadLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (shouldStartObserve()) {
                ComponentName componentName = PackageUtil.getTopActivity(mContext);
                if (componentName != null) {
                    String oldPackage = mOldComponentName == null ? null : mOldComponentName.getPackageName();
                    String newPackage = componentName.getPackageName();
                    if (!TextUtils.equals(oldPackage, newPackage)) {
                        for (TopActivityChangedListener listener : mListeners) {
                            listener.onPackageChanged(mOldComponentName, componentName);
                        }
                    }
                    mOldComponentName = componentName;
                }
                sendEmptyMessageDelayed(MSG_ID, 200);
            }
        }
    }

    /*private class ListenersHandler extends Handler {

        static final int MSG_ID = 100;

        private ComponentName mOldComponentName;

        ListenersHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object obj = msg.obj;
            if (obj == null || !(obj instanceof ComponentName[])) return;
            ComponentName[] componentNames = (ComponentName[]) msg.obj;
            if (componentNames.length < 2) return;
            ComponentName oldActivity = componentNames[0];
            ComponentName newActivity = componentNames[1];

            String oldPackage = mOldComponentName == null ? null : mOldComponentName.getPackageName();



            for (TopActivityChangedListener listener : mListeners) {

            }
        }

        void postPackageChanged(ComponentName oldActivity, ComponentName newActivity) {
            removeMessages(MSG_ID);
            Message message = obtainMessage(MSG_ID, new ComponentName[]{oldActivity, newActivity});
            sendMessage(message);
        }
    }*/

    public interface TopActivityChangedListener {

        void onPackageChanged(ComponentName oldActivity, ComponentName newActivity);

    }
}