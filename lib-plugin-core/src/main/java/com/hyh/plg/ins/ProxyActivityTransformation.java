package com.hyh.plg.ins;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hyh.plg.activity.transform.TransformIntent;
import com.hyh.plg.activity.transform.Transformation;
import com.hyh.plg.core.ParamsKey;
import com.hyh.plg.utils.PackageUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2020/4/26
 */
class ProxyActivityTransformation implements Transformation {

    private static final String TRANSFORM_FLAG = "__ylyread_transformation__";

    public static final String ORIGINAL_INTENT_KEY = "__ylyread_original_intent__";

    private final Context mContext;
    private final String mStandardActivityPath;

    private final Map<String, Boolean> mActivityRegisterResult = new HashMap<>();

    ProxyActivityTransformation(Context context, Map<String, Object> params) {
        this.mContext = context;
        this.mStandardActivityPath = (String) params.get(ParamsKey.STANDARD_ACTIVITY_PATH);
    }

    @Override
    public TransformIntent transform(Intent intent) {
        if (intent == null || intent.getBooleanExtra(TRANSFORM_FLAG, false)) return null;
        intent.putExtra(TRANSFORM_FLAG, true);
        ComponentName component = intent.getComponent();
        if (component == null) {
            return null;
        }
        String packageName = component.getPackageName();

        if (!TextUtils.equals(packageName, mContext.getPackageName())) {
            return null;
        }
        String className = component.getClassName();
        if (isActivityRegistered(className)) {
            return null;
        }

        int flags = intent.getFlags();
        int transformFlags = Intent.FLAG_ACTIVITY_NEW_TASK;
        if ((flags & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) != 0) {
            transformFlags |= Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
        }
        if ((flags & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0) {
            transformFlags |= Intent.FLAG_ACTIVITY_CLEAR_TOP;
        }
        if ((flags & Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0) {
            transformFlags |= Intent.FLAG_ACTIVITY_SINGLE_TOP;
        }

        Intent transformIntent = new Intent();
        transformIntent.setAction(String.valueOf(System.currentTimeMillis()));

        String intentKey = String.valueOf(System.identityHashCode(transformIntent));
        IntentCache.getInstance().saveIntent(intentKey, intent);
        transformIntent.putExtra(ORIGINAL_INTENT_KEY, intentKey);
        transformIntent.addFlags(transformFlags);
        transformIntent.setClassName(mContext.getPackageName(), mStandardActivityPath);

        return new TransformIntent(intent, transformIntent);
    }

    private boolean isActivityRegistered(String className) {
        Boolean result = mActivityRegisterResult.get(className);
        if (result != null) return result;
        result = PackageUtils.isActivityExist(mContext, className);
        if (result != null) {
            mActivityRegisterResult.put(className, result);
            return result;
        }
        return false;
    }
}