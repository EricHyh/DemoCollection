package com.hyh.plg.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * @author Administrator
 * @description
 * @data 2019/2/27
 */

public class BridgeFactory implements IBridgeFactory {

    private static IBridgeFactory sInstance;

    public static IBridgeFactory getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (BridgeFactory.class) {
            if (sInstance == null) {
                sInstance = new BridgeFactory(context);
            }
            return sInstance;
        }
    }

    private final Context mContext;

    private BridgeFactory(Context context) {
        this.mContext = context;
    }

    @Override
    public Context replaceBaseContext(Activity activity, Context newBase) {
        return newBase;
    }

    @Override
    public IActivityBridge produce(Activity activity, Bundle savedInstanceState) {
        Intent intent = activity.getIntent();
        if (intent == null) return null;
        Bundle bundle = intent.getBundleExtra(ActivityBridgeManager.ACTIVITY_BRIDGE_EXTRA_KEY);
        if (bundle == null) return null;
        String hashStr = bundle.getString(ActivityBridgeManager.ACTIVITY_BRIDGE_HASH_KEY);
        if (TextUtils.isEmpty(hashStr)) return null;
        return ActivityBridgeManager.getInstance().removeActivityBridge(hashStr);
    }
}