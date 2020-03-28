package com.hyh.common.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Administrator
 * @description
 * @data 2018/11/5
 */

public class HomeKeyReceiver extends BroadcastReceiver {

    final String SYSTEM_DIALOG_REASON_KEY = "reason";

    final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private static HomeKeyReceiver sHomeKeyReceiver;

    public static HomeKeyReceiver getInstance(Context context) {
        if (sHomeKeyReceiver != null) {
            sHomeKeyReceiver.tryToRegister(context.getApplicationContext());
            return sHomeKeyReceiver;
        }
        synchronized (HomeKeyReceiver.class) {
            if (sHomeKeyReceiver == null) {
                sHomeKeyReceiver = new HomeKeyReceiver(context.getApplicationContext());
            }
        }
        return sHomeKeyReceiver;
    }

    private CopyOnWriteArrayList<HomeKeyListener> mHomeKeyListeners = new CopyOnWriteArrayList<>();

    private boolean isRegister;

    private HomeKeyReceiver(Context context) {
        tryToRegister(context);
    }

    private void tryToRegister(Context context) {
        if (isRegister) {
            return;
        }
        try {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.registerReceiver(this, intentFilter);
            isRegister = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(HomeKeyListener listener) {
        if (listener == null || mHomeKeyListeners.contains(listener)) {
            return;
        }
        mHomeKeyListeners.add(listener);
    }

    public void removeListener(HomeKeyListener listener) {
        mHomeKeyListeners.remove(listener);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (mHomeKeyListeners.isEmpty()) {
            return;
        }
        String action = intent.getAction();
        String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
        if (TextUtils.equals(action, Intent.ACTION_CLOSE_SYSTEM_DIALOGS) && TextUtils.equals(reason, SYSTEM_DIALOG_REASON_HOME_KEY)) {
            for (HomeKeyListener listener : mHomeKeyListeners) {
                if (listener != null) {
                    listener.onHomeKeyClick();
                }
            }
        }
    }
}
