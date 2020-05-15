package com.hyh.plg.hook.instrumentation;


import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.os.Bundle;
import android.util.SparseArray;

import com.hyh.plg.android.BlockApplication;
import com.hyh.plg.reflect.RefAction;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 * @description
 * @data 2019/6/26
 */

public class ActivityInstrumentation extends BlockInstrumentation {

    private static final SparseArray<ActivityInstrumentation> sInstrumentationArray = new SparseArray<>();

    public static ActivityInstrumentation obtain(Activity activity) {
        synchronized (sInstrumentationArray) {
            int hashCode = System.identityHashCode(activity);
            ActivityInstrumentation instrumentation = sInstrumentationArray.get(hashCode);
            if (instrumentation != null) {
                return instrumentation;
            }
            registerActivity();
            instrumentation = new ActivityInstrumentation(activity);
            sInstrumentationArray.put(hashCode, instrumentation);
            return instrumentation;
        }
    }

    private static void registerActivity() {
        BlockApplication.blockApplication.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksImpl());
    }

    private final Object mLock = new Object();

    private final AtomicInteger mReplacedTimes = new AtomicInteger(0);

    private WeakReference<Activity> mActivityRef;

    private volatile boolean mIsReplaced;

    private ActivityInstrumentation(Activity activity) {
        this.mActivityRef = new WeakReference<>(activity);
    }

    @Override
    public void requestReplaceInstrumentation() {
        synchronized (mLock) {
            Activity activity = mActivityRef.get();
            if (activity == null || activity.isFinishing()) {
                Logger.d("ActivityInstrumentation requestReplaceInstrumentation activity is null or finished");
                return;
            }
            final Instrumentation original = getInstrumentation(activity);
            if (original == null) {
                Logger.d("ActivityInstrumentation requestReplaceInstrumentation original is null");
                return;
            }

            mReplacedTimes.incrementAndGet();
            if (mIsReplaced) {
                Logger.d("ActivityInstrumentation requestReplaceInstrumentation is replaced");
                return;
            }
            if (original == ActivityInstrumentation.this) {
                Logger.d("ActivityInstrumentation requestReplaceInstrumentation original == this");
                mIsReplaced = true;
                return;
            }

            Logger.d("ActivityInstrumentation requestReplaceInstrumentation start replace");
            setBase(original);
            Reflect.from(Activity.class)
                    .filed("mInstrumentation", Instrumentation.class)
                    .resultAction(new RefAction<Instrumentation>() {
                        @Override
                        public void onSuccess() {
                            Logger.d("ActivityInstrumentation requestReplaceInstrumentation replace success");
                            mIsReplaced = true;
                            Class<? extends Instrumentation> originalClass = original.getClass();
                            Logger.d("ActivityInstrumentation requestReplaceInstrumentation originalClass is" + originalClass);
                            if (originalClass.getName().startsWith("com.yly")) {
                                Reflect.from(originalClass)
                                        .method("onInstrumentationReplaced")
                                        .param(Instrumentation.class, ActivityInstrumentation.this)
                                        .invoke(original);
                            }
                        }
                    })
                    .set(activity, ActivityInstrumentation.this);
        }
    }

    @Override
    public void requestRecoverInstrumentation() {
        synchronized (mLock) {
            final Activity activity = mActivityRef.get();
            if (activity == null || activity.isFinishing()) {
                Logger.d("ActivityInstrumentation requestRecoverInstrumentation activity is null or finished");
                return;
            }

            Instrumentation original = getInstrumentation(activity);
            if (original == null) {
                Logger.d("ActivityInstrumentation requestRecoverInstrumentation activity mInstrumentation is null");
                return;
            }
            if (!mIsReplaced) {
                Logger.d("ActivityInstrumentation requestRecoverInstrumentation is not replaced");
                return;
            }
            int replaceTimes = mReplacedTimes.get();
            if (replaceTimes > 0) {
                replaceTimes = mReplacedTimes.decrementAndGet();
            }

            Logger.d("ActivityInstrumentation requestRecoverInstrumentation replaceTimes = " + replaceTimes);
            if (replaceTimes <= 0) {
                if (original == ActivityInstrumentation.this) {
                    final Instrumentation base = mBase;
                    Reflect.from(Activity.class)
                            .filed("mInstrumentation", Instrumentation.class)
                            .resultAction(new RefAction<Instrumentation>() {
                                @Override
                                public void onSuccess() {
                                    Logger.d("ActivityInstrumentation requestReplaceInstrumentation recover success");
                                    mIsReplaced = false;
                                    synchronized (sInstrumentationArray) {
                                        int hashCode = System.identityHashCode(activity);
                                        sInstrumentationArray.remove(hashCode);
                                    }
                                    Class<? extends Instrumentation> baseClass = base.getClass();
                                    Logger.d("ActivityInstrumentation requestReplaceInstrumentation baseClass is" + baseClass);
                                    if (baseClass.getName().startsWith("com.yly")) {
                                        Reflect.from(baseClass).method("onInstrumentationRecovered").invoke(base);
                                    }
                                }
                            })
                            .set(activity, base);
                }
            }
        }
    }

    @Override
    public void onInstrumentationReplaced(Instrumentation newInstrumentation) {
        Logger.d("ActivityInstrumentation [" + this + "] onInstrumentationReplaced, newInstrumentation is " + newInstrumentation);
    }

    @Override
    public void onInstrumentationRecovered() {
        Logger.d("ActivityInstrumentation [" + this + "] onInstrumentationRecovered");
        int replaceTimes = mReplacedTimes.get();
        if (replaceTimes <= 0) {
            requestRecoverInstrumentation();
        }
    }

    private Instrumentation getInstrumentation(Activity activity) {
        return Reflect.from(Activity.class).filed("mInstrumentation", Instrumentation.class).get(activity);
    }

    private static class ActivityLifecycleCallbacksImpl implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            synchronized (sInstrumentationArray) {
                int hashCode = System.identityHashCode(activity);
                ActivityInstrumentation activityInstrumentation = sInstrumentationArray.get(hashCode);
                if (activityInstrumentation != null) {
                    sInstrumentationArray.remove(hashCode);
                }
            }
        }
    }
}