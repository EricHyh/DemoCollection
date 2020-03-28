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
 * @data 2018/7/24
 */

public class UserPresentReceiver extends BroadcastReceiver {

    private static UserPresentReceiver sUserPresentReceiver;

    public static UserPresentReceiver getInstance(Context context) {
        context = context.getApplicationContext();
        if (sUserPresentReceiver != null) {
            sUserPresentReceiver.tryToRegister(context);
            return sUserPresentReceiver;
        }
        synchronized (UserPresentReceiver.class) {
            if (sUserPresentReceiver == null) {
                sUserPresentReceiver = new UserPresentReceiver(context);
            }
        }
        return sUserPresentReceiver;
    }

    private boolean isRegister;

    private CopyOnWriteArrayList<UserPresentListener> mUserPresentListeners = new CopyOnWriteArrayList<>();

    private UserPresentReceiver(Context context) {
        tryToRegister(context);
    }

    private void tryToRegister(Context context) {
        if (isRegister) {
            return;
        }
        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            context.registerReceiver(this, intentFilter);
            isRegister = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(UserPresentListener listener) {
        if (listener == null || mUserPresentListeners.contains(listener)) {
            return;
        }
        mUserPresentListeners.add(listener);
    }

    public void addFirstListener(UserPresentListener listener) {
        if (listener == null || mUserPresentListeners.contains(listener)) {
            return;
        }
        mUserPresentListeners.add(0, listener);
    }

    public void removeListener(UserPresentListener listener) {
        mUserPresentListeners.remove(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_USER_PRESENT)) {
            if (mUserPresentListeners.isEmpty()) {
                return;
            }
            for (UserPresentListener userPresentListener : mUserPresentListeners) {
                if (userPresentListener != null) {
                    userPresentListener.onUserPresent();
                }
            }
        }
    }
}
