package com.hyh.plg.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import com.yly.mob.utils.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by tangdongwei on 2018/10/19.
 */
@SuppressLint("UseSparseArrays")
public class ActivityLaunchRecord {

    private static Map<Integer, ActivityLaunchRecordInfo> mActivityLaunchRecordInfoMap;

    public static final Object sActivityLaunchRecordLock = new Object();

    static {
        mActivityLaunchRecordInfoMap = new HashMap<>();
    }

    public static void recordActivityLaunchInfo(Activity activity) {
        synchronized (sActivityLaunchRecordLock) {
            Intent intent = activity.getIntent();
            if (intent != null) {
                int key = activity.hashCode();
                Logger.d("IN ActivityLaunchRecord, recordActivityLaunchInfo(), key=" + key + " , record : " + intent);
                int launchMode = intent.getIntExtra(ActivityProxyImpl.EXTRA_LAUNCH_MODE, -1);
                ActivityLaunchRecordInfo activityLaunchRecordInfo = new ActivityLaunchRecordInfo();
                activityLaunchRecordInfo.key = key;
                activityLaunchRecordInfo.launchMode = launchMode;
                mActivityLaunchRecordInfoMap.put(key, activityLaunchRecordInfo);
            }
        }
    }

    public static void removeActivityLaunchInfo(Activity activity) {
        synchronized (sActivityLaunchRecordLock) {
            int key = activity.hashCode();
            Logger.d("IN ActivityLaunchRecord, removeActivityLaunchInfo(), key=" + key);
            mActivityLaunchRecordInfoMap.remove(key);
        }
    }

    public static boolean hasLaunchModeRunning(int launchMode) {
        synchronized (sActivityLaunchRecordLock) {
            if (mActivityLaunchRecordInfoMap.isEmpty()) {
                return false;
            }
            Set<Integer> keySet = mActivityLaunchRecordInfoMap.keySet();
            if (keySet.isEmpty()) {
                return false;
            }
            Iterator<Integer> iterator = keySet.iterator();
            ActivityLaunchRecordInfo activityLaunchRecordInfo;
            Integer key;
            while (iterator.hasNext()) {
                key = iterator.next();
                activityLaunchRecordInfo = mActivityLaunchRecordInfoMap.get(key);
                if (activityLaunchRecordInfo != null && activityLaunchRecordInfo.launchMode == launchMode) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class ActivityLaunchRecordInfo {
        public int key;
        public int launchMode;
    }
}