package com.hyh.common.receiver;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * @author Administrator
 * @description
 * @data 2018/11/21
 */

public class ActivityHomeKeyListener implements HomeKeyListener {

    private WeakReference<Activity> mActivityRef;

    public ActivityHomeKeyListener(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    @Override
    public void onHomeKeyClick() {
        Activity activity = mActivityRef.get();
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }
}