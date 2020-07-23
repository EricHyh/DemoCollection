package com.hyh.plg.ins;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.text.TextUtils;

import com.hyh.plg.activity.transform.TransformIntent;
import com.hyh.plg.api.BlockEnv;
import com.hyh.plg.reflect.RefResult;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2020/4/24
 */
public class BlockInstrumentation extends InstrumentationDelegate {

    private final ProxyActivityTransformation mTransformation;

    public BlockInstrumentation(Context context, Instrumentation base, Map<String, Object> params) {
        setBase(base);
        this.mTransformation = new ProxyActivityTransformation(context, params);
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String target,
            Intent intent, int requestCode, Bundle options) throws Throwable {
        Logger.d("IN BlockInstrumentation, execStartActivity 1(), target=" + target + " , intent=" + intent);
        TransformIntent transformIntent = mTransformation.transform(intent);
        if (transformIntent != null) {
            intent = transformIntent.transformIntent;
        }
        if (intent == null) {
            return null;
        }
        RefResult<ActivityResult> result = new RefResult<>();
        ActivityResult activityResult = Reflect.from(Instrumentation.class)
                .method("execStartActivity", ActivityResult.class)
                .param(Context.class, who)
                .param(IBinder.class, contextThread)
                .param(IBinder.class, token)
                .param(String.class, target)
                .param(Intent.class, intent)
                .param(int.class, requestCode)
                .param(Bundle.class, options)
                .saveResult(result)
                .invoke(mBase);
        if (result.isSuccess()) {
            return activityResult;
        } else {
            Throwable throwable = result.getThrowable();
            if (isExceptionCauseBySelf(throwable)) {
                Logger.e("IN BlockInstrumentation, execStartActivity 1(), ERROR : ", throwable);
            } else {
                throw throwable;
            }
        }
        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) throws Throwable {
        Logger.d("IN BlockInstrumentation, execStartActivity 2(), intent=" + intent);

        TransformIntent transformIntent = mTransformation.transform(intent);
        if (transformIntent != null) {
            intent = transformIntent.transformIntent;
        }
        if (intent == null) {
            return null;
        }
        RefResult<ActivityResult> result = new RefResult<>();
        ActivityResult activityResult = Reflect.from(Instrumentation.class)
                .method("execStartActivity", ActivityResult.class)
                .param(Context.class, who)
                .param(IBinder.class, contextThread)
                .param(IBinder.class, token)
                .param(Activity.class, target)
                .param(Intent.class, intent)
                .param(int.class, requestCode)
                .param(Bundle.class, options)
                .saveResult(result)
                .invoke(mBase);
        if (result.isSuccess()) {
            return activityResult;
        } else {
            Throwable throwable = result.getThrowable();
            if (isExceptionCauseBySelf(throwable)) {
                Logger.e("IN BlockInstrumentation, execStartActivity 2(), ERROR : ", throwable);
            } else {
                throw throwable;
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String resultWho,
            Intent intent, int requestCode, Bundle options, UserHandle user) throws Throwable {
        Logger.d("IN BlockInstrumentation, execStartActivity 3(), intent=" + intent);

        TransformIntent transformIntent = mTransformation.transform(intent);
        if (transformIntent != null) {
            intent = transformIntent.transformIntent;
        }
        if (intent == null) {
            return null;
        }

        RefResult<ActivityResult> result = new RefResult<>();
        ActivityResult activityResult = Reflect.from(Instrumentation.class)
                .method("execStartActivity", ActivityResult.class)
                .param(Context.class, who)
                .param(IBinder.class, contextThread)
                .param(IBinder.class, token)
                .param(String.class, resultWho)
                .param(Intent.class, intent)
                .param(int.class, requestCode)
                .param(Bundle.class, options)
                .param(UserHandle.class, user)
                .saveResult(result)
                .invoke(mBase);
        if (result.isSuccess()) {
            return activityResult;
        } else {
            Throwable throwable = result.getThrowable();
            if (isExceptionCauseBySelf(throwable)) {
                Logger.e("IN BlockInstrumentation, execStartActivity 3(), ERROR : ", throwable);
            } else {
                throw throwable;
            }
        }
        return null;
    }

    private boolean isExceptionCauseBySelf(Throwable throwable) {
        return (throwable instanceof NoSuchMethodException
                || throwable instanceof NullPointerException
                || throwable instanceof SecurityException
                || throwable instanceof IllegalAccessException
                || throwable instanceof IllegalArgumentException);
    }


    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String originalIntentKey = intent.getStringExtra(ProxyActivityTransformation.ORIGINAL_INTENT_KEY);
        Logger.d("BlockInstrumentation newActivity: " +
                "className = " + className +
                ", intent = " + intent +
                ", originalIntentKey = " + originalIntentKey);
        if (!TextUtils.isEmpty(originalIntentKey)) {
            Intent originalIntent = IntentCache.getInstance().getIntent(originalIntentKey);
            if (originalIntent != null) {
                originalIntent.setExtrasClassLoader(BlockInstrumentation.class.getClassLoader());
                ComponentName component = originalIntent.getComponent();
                assert component != null;
                return (Activity) Class.forName(component.getClassName()).newInstance();
            }
        }
        return super.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        fixIntent(activity, activity.getIntent());


        /*Reflect.from(Activity.class).filed("mInflater").set(activity, null);
        Reflect.from(Activity.class).filed("mTheme").set(activity, null);
        Reflect.from(Activity.class).filed("mResources").set(activity, null);*/

        //Reflect.from(Activity.class).filed("mInflater").set(activity, null);
        Reflect.from(Activity.class).filed("mTheme").set(activity, null);
        Reflect.from(Activity.class).filed("mResources").set(activity, null);
        /*Window window = activity.getWindow();
        Reflect.from(window.getClass()).filed("mLayoutInflater").set(window, activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));*/


        ActivityInfo activityInfo = Reflect.from(Activity.class)
                .filed("mActivityInfo", ActivityInfo.class)
                .get(activity);
        if (activityInfo != null) {
            activityInfo.applicationInfo.icon = BlockEnv.sBlockPackageInfo.applicationInfo.icon;
            activityInfo.applicationInfo.labelRes = BlockEnv.sBlockPackageInfo.applicationInfo.labelRes;
        }

        ActivityInfo blockActivityInfo = BlockEnv.getActivityInfo(activity);

        if (blockActivityInfo != null) {
            int theme = blockActivityInfo.theme;
            if (theme == 0) {
                theme = BlockEnv.sBlockPackageInfo.applicationInfo.theme;
            }
            activity.setTheme(theme);

            int screenOrientation = blockActivityInfo.screenOrientation;
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    && screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                activity.setRequestedOrientation(screenOrientation);
            }

            int softInputMode = blockActivityInfo.softInputMode;
            activity.getWindow().setSoftInputMode(softInputMode);
        }

        super.callActivityOnCreate(activity, icicle);
    }

    private void fixIntent(Activity activity, Intent intent) {
        String originalIntentKey = intent.getStringExtra(ProxyActivityTransformation.ORIGINAL_INTENT_KEY);
        if (!TextUtils.isEmpty(originalIntentKey)) {
            Intent originalIntent = IntentCache.getInstance().removeIntent(originalIntentKey);
            if (originalIntent != null) {
                originalIntent.setExtrasClassLoader(BlockInstrumentation.class.getClassLoader());
                activity.setIntent(originalIntent);
            }
        }
    }
}