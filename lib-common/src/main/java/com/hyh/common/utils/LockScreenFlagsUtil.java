package com.hyh.common.utils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.hyh.common.log.Logger;


/**
 * @author Administrator
 * @description
 * @data 2019/9/7
 */

public class LockScreenFlagsUtil {

    public static void handle(Activity activity, boolean showWhenLocked, boolean dismissKeyguard) {
        Window window = activity.getWindow();
        if (showWhenLocked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            if (Build.VERSION.SDK_INT >= 27) {
                try {
                    activity.setShowWhenLocked(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (dismissKeyguard) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!PhoneStateUtil.isDeviceSecureCompat(activity)) {
                    KeyguardManager systemService = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
                    if (systemService != null) {
                        systemService.requestDismissKeyguard(activity, new KeyguardManager.KeyguardDismissCallback() {
                            @Override
                            public void onDismissError() {
                                Logger.d("onDismissError");
                            }

                            @Override
                            public void onDismissSucceeded() {
                                Logger.d("onDismissSucceeded");
                            }

                            @Override
                            public void onDismissCancelled() {
                                Logger.d("onDismissCancelled");
                            }
                        });
                    }
                }
            }
        }
    }

    public static void requestDismissKeyguard(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PhoneStateUtil.inKeyguardRestrictedInputMode(activity)) {
                try {
                    KeyguardManager systemService = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
                    if (systemService != null) {
                        systemService.requestDismissKeyguard(activity, new KeyguardManager.KeyguardDismissCallback() {
                            @Override
                            public void onDismissError() {
                                Logger.d("onDismissError");
                            }

                            @Override
                            public void onDismissSucceeded() {
                                Logger.d("onDismissSucceeded");
                            }

                            @Override
                            public void onDismissCancelled() {
                                Logger.d("onDismissCancelled");
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}