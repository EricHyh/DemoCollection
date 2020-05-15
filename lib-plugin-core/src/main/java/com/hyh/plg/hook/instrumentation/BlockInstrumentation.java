package com.hyh.plg.hook.instrumentation;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.text.TextUtils;

import com.hyh.plg.activity.ActivityProxyImpl;
import com.hyh.plg.activity.ActivityProxyInner2;
import com.hyh.plg.activity.IntentDataCarrier;
import com.hyh.plg.activity.transform.TransformIntent;
import com.hyh.plg.reflect.RefResult;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

/**
 * @author Administrator
 * @description
 * @data 2019/6/26
 */
@SuppressLint("PrivateApi")
abstract class BlockInstrumentation extends InstrumentationDelegate implements IInstrumentation {

    @Override
    public int getInstrumentationVersion() {
        return 1;
    }

    public Instrumentation.ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String target,
            Intent intent, int requestCode, Bundle options) throws Throwable {
        Logger.d("IN BlockInstrumentation, execStartActivity 1(), target=" + target + " , intent=" + intent);
        TransformIntent transformIntent = ActivityProxyImpl.transformIntentForActivityProxy(intent);
        if (transformIntent != null) {
            intent = transformIntent.transformIntent;
        }
        if (intent == null) {
            return null;
        }
        ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
        RefResult<Instrumentation.ActivityResult> result = new RefResult<>();
        Instrumentation.ActivityResult activityResult = Reflect.from(Instrumentation.class)
                .method("execStartActivity", Instrumentation.ActivityResult.class)
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

    public Instrumentation.ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) throws Throwable {
        Logger.d("IN BlockInstrumentation, execStartActivity 2(), intent=" + intent);

        TransformIntent transformIntent = ActivityProxyImpl.transformIntentForActivityProxy(intent);
        if (transformIntent != null) {
            intent = transformIntent.transformIntent;
        }
        if (intent == null) {
            return null;
        }
        ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
        RefResult<Instrumentation.ActivityResult> result = new RefResult<>();
        Instrumentation.ActivityResult activityResult = Reflect.from(Instrumentation.class)
                .method("execStartActivity", Instrumentation.ActivityResult.class)
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Instrumentation.ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String resultWho,
            Intent intent, int requestCode, Bundle options, UserHandle user) throws Throwable {
        Logger.d("IN BlockInstrumentation, execStartActivity 3(), intent=" + intent);

        TransformIntent transformIntent = ActivityProxyImpl.transformIntentForActivityProxy(intent);
        if (transformIntent != null) {
            intent = transformIntent.transformIntent;
        }
        if (intent == null) {
            return null;
        }
        ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
        RefResult<Instrumentation.ActivityResult> result = new RefResult<>();
        Instrumentation.ActivityResult activityResult = Reflect.from(Instrumentation.class)
                .method("execStartActivity", Instrumentation.ActivityResult.class)
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
        return throwable != null &&
                (throwable instanceof NoSuchMethodException
                        || throwable instanceof NullPointerException
                        || throwable instanceof SecurityException
                        || throwable instanceof IllegalAccessException
                        || throwable instanceof IllegalArgumentException);
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Logger.d("IN BlockInstrumentation newActivity, className=" + className + " , intent=" + intent);
        if (!TextUtils.isEmpty(className) && intent != null) {
            String data = intent.getDataString();
            if (!TextUtils.isEmpty(data)) {
                IntentDataCarrier intentDataCarrier = IntentDataCarrier.create(data);
                if (intentDataCarrier != null && intentDataCarrier.isFromBlock() && intentDataCarrier.isFromCurrentClassLoader()) {
                    if (intentDataCarrier.isOpenBlockActivity()) {
                        return (Activity) Class.forName(intentDataCarrier.getBlockActivityClassPath()).newInstance();
                    } else {
                        return new ActivityProxyInner2();
                    }
                }
            }
        }
        return super.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if (ActivityProxyImpl.isBlockActivity(activity)) {
            ActivityProxyImpl.fixBlockActivityContext(activity);
            ActivityProxyImpl.fixActivityWindow(activity);
            ActivityProxyImpl.fixActivityIntent(activity);
            ActivityProxyImpl.fixActivityInstrumentation(activity);
        }
        super.callActivityOnCreate(activity, icicle);
    }
}