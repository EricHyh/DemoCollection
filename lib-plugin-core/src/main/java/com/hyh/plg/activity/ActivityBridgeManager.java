package com.hyh.plg.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * @author Administrator
 * @description
 * @data 2019/2/27
 */
@SuppressLint("StaticFieldLeak")
public class ActivityBridgeManager {

    public static final String DEFAULT_NONTRANSPARENT_BRIDGE_ACTIVITY_PATH = "default_nontransparent_bridge_activity_path";
    public static final String DEFAULT_TRANSPARENT_BRIDGE_ACTIVITY_PATH = "default_transparent_bridge_activity_path";
    public static final String NONTRANSPARENT_BRIDGE_ACTIVITY_PATH = "nontransparent_bridge_activity_path";
    public static final String TRANSPARENT_BRIDGE_ACTIVITY_PATH = "transparent_bridge_activity_path";

    public static final String ACTIVITY_BRIDGE_HASH_KEY = "activity_bridge_hash_key";
    public static final String ACTIVITY_BRIDGE_EXTRA_KEY = "activity_bridge_extra_key";

    private static ActivityBridgeManager sInstance = new ActivityBridgeManager();

    public static ActivityBridgeManager getInstance() {
        return sInstance;
    }

    private final HashMap<String, IActivityBridge> mProxyMap = new HashMap<>();

    public boolean isSupportTransparentActivity() {
        return !TextUtils.isEmpty(ActivityBridgeProxyInfo.sTransparentActivityPath);
    }

    public void startActivity(IActivityBridge activityBridge) {
        startActivity(activityBridge, null, null, false);
    }

    public void startActivity(IActivityBridge activityBridge, boolean transparent) {
        startActivity(activityBridge, null, null, transparent);
    }

    public void startActivity(IActivityBridge activityBridge, String bundleKey, Bundle outerBundle, boolean transparent) {
        if (activityBridge == null) {
            return;
        }
        int hashCode = System.identityHashCode(activityBridge);
        String hashStr = String.valueOf(hashCode);
        IActivityBridge cacheViewProxy = mProxyMap.get(hashStr);
        if (cacheViewProxy != null) {
            return;
        }
        mProxyMap.put(hashStr, activityBridge);

        Bundle bundle = new Bundle();
        bundle.putString(ACTIVITY_BRIDGE_HASH_KEY, hashStr);
        String actPath = isSupportTransparentActivity() && transparent ? ActivityBridgeProxyInfo.sTransparentActivityPath :
                ActivityBridgeProxyInfo.sNontransparentActivityPath;
        if (!TextUtils.isEmpty(actPath)) {//使用静态代理的Activity
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(ActivityBridgeProxyInfo.sHostContext, actPath));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(bundleKey) && outerBundle != null) {
                if (TextUtils.equals(ACTIVITY_BRIDGE_EXTRA_KEY, bundleKey)) {
                    bundle.putAll(outerBundle);
                } else {
                    intent.putExtra(bundleKey, outerBundle);
                }
            }
            intent.putExtra(ACTIVITY_BRIDGE_EXTRA_KEY, bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            ActivityBridgeProxyInfo.sHostContext.startActivity(intent);
        } else {//使用动态代理的Activity
            //com.yly.mob.activity.ActivityBridgeImpl
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("", ActivityBridgeImpl.class.getName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(bundleKey) && outerBundle != null) {
                if (TextUtils.equals(ACTIVITY_BRIDGE_EXTRA_KEY, bundleKey)) {
                    bundle.putAll(outerBundle);
                } else {
                    intent.putExtra(bundleKey, outerBundle);
                }
            }
            intent.putExtra(ACTIVITY_BRIDGE_EXTRA_KEY, bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            ActivityBridgeProxyInfo.sBlockContext.startActivity(intent);
        }
    }

    public Intent getStartActivityIntent(IActivityBridge activityBridge, String bundleKey, Bundle outerBundle, boolean transparent) {
        if (activityBridge == null) {
            return null;
        }
        int hashCode = System.identityHashCode(activityBridge);
        String hashStr = String.valueOf(hashCode);
        mProxyMap.put(hashStr, activityBridge);

        Bundle bundle = new Bundle();
        bundle.putString(ACTIVITY_BRIDGE_HASH_KEY, hashStr);
        String actPath = isSupportTransparentActivity() && transparent ? ActivityBridgeProxyInfo.sTransparentActivityPath :
                ActivityBridgeProxyInfo.sNontransparentActivityPath;

        Intent intent;
        if (!TextUtils.isEmpty(actPath)) {//使用静态代理的Activity
            intent = new Intent();
            intent.setComponent(new ComponentName(ActivityBridgeProxyInfo.sHostContext, actPath));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(bundleKey) && outerBundle != null) {
                if (TextUtils.equals(ACTIVITY_BRIDGE_EXTRA_KEY, bundleKey)) {
                    bundle.putAll(outerBundle);
                } else {
                    intent.putExtra(bundleKey, outerBundle);
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.putExtra(ACTIVITY_BRIDGE_EXTRA_KEY, bundle);
        } else {
            intent = new Intent();
            intent.setComponent(new ComponentName("", ActivityBridgeImpl.class.getName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(bundleKey) && outerBundle != null) {
                if (TextUtils.equals(ACTIVITY_BRIDGE_EXTRA_KEY, bundleKey)) {
                    bundle.putAll(outerBundle);
                } else {
                    intent.putExtra(bundleKey, outerBundle);
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.putExtra(ACTIVITY_BRIDGE_EXTRA_KEY, bundle);
        }
        return intent;
    }

    IActivityBridge removeActivityBridge(String hashStr) {
        return mProxyMap.remove(hashStr);
    }
}