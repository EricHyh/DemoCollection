package com.hyh.plg.hook.instrumentation;


import android.app.Instrumentation;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hyh.plg.reflect.RefAction;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 * @description
 * @data 2019/6/26
 */

public class ApplicationInstrumentation extends BlockInstrumentation {

    private static final ApplicationInstrumentation sInstance = new ApplicationInstrumentation();

    public static ApplicationInstrumentation getInstance() {
        return sInstance;
    }

    private final Object mLock = new Object();

    private final AtomicInteger mReplacePersistTimes = new AtomicInteger(0);

    private final RecoverHandler mRecoverHandler = new RecoverHandler();

    private Object mActivityThread;

    private volatile boolean mIsRequestRecoverInstrumentation;

    private ApplicationInstrumentation() {
    }

    @Override
    public void requestReplaceInstrumentation() {
        synchronized (mLock) {
            removeRecoverTask();

            mIsRequestRecoverInstrumentation = false;

            Object activityThread = getActivityThread();
            if (activityThread == null) {
                Logger.d("ApplicationInstrumentation requestReplaceInstrumentation activityThread is null");
                return;
            }
            final Instrumentation original = getSystemInstrumentation();
            if (original == null) {
                Logger.d("ApplicationInstrumentation requestReplaceInstrumentation original is null");
                return;
            }

            if (mBase == null) {
                Logger.d("ApplicationInstrumentation requestReplaceInstrumentation start replace");
                setBase(original);
                executeReplace(activityThread, original);
            } else {
                if (original == ApplicationInstrumentation.this) {
                    Logger.d("ApplicationInstrumentation requestReplaceInstrumentation original == this");
                    postRecoverTaskIfNecessary();
                } else if (original == mBase) {
                    Logger.d("ApplicationInstrumentation requestReplaceInstrumentation original == base");
                    executeReplace(activityThread, original);
                } else if (original.getClass().getName().startsWith("com.yly")) {
                    //mBase不为null，且当前的original是其他产品的对象，说明有其他产品替换了Instrumentation，此时就不需要再替换了，避免出现递归替换
                    postRecoverTaskIfNecessary();
                    Logger.d("ActivityThread instrumentation has been hooked by another product");
                } else {
                    //有其他人Hook了Instrumentation，这种情况下就不需要恢复了
                    Logger.d("ActivityThread instrumentation has been hooked by others, " +
                            "current instrumentation is " + original);
                }
            }
        }
    }

    private void executeReplace(Object activityThread, final Instrumentation original) {
        Reflect.from("android.app.ActivityThread")
                .filed("mInstrumentation", Instrumentation.class)
                .resultAction(new RefAction<Instrumentation>() {
                    @Override
                    public void onSuccess() {
                        Class<? extends Instrumentation> originalClass = original.getClass();
                        Logger.d("ApplicationInstrumentation executeReplace replace success, " +
                                "originalClass = " + originalClass);
                        if (originalClass.getName().startsWith("com.yly")) {
                            Reflect.from(originalClass)
                                    .method("onInstrumentationReplaced")
                                    .param(Instrumentation.class, ApplicationInstrumentation.this)
                                    .invoke(original);
                        }
                        postRecoverTaskIfNecessary();
                    }
                })
                .set(activityThread, ApplicationInstrumentation.this);
    }

    private void postRecoverTaskIfNecessary() {
        if (mReplacePersistTimes.get() <= 0) {
            mRecoverHandler.postRecoverMessage();
        }
    }

    private void removeRecoverTask() {
        mRecoverHandler.removeRecoverMessage();
    }

    @Override
    public void requestRecoverInstrumentation() {
        synchronized (mLock) {
            mIsRequestRecoverInstrumentation = true;
            Object activityThread = getActivityThread();
            if (activityThread == null) {
                return;
            }
            Instrumentation original = getSystemInstrumentation();
            if (original == null) {
                Logger.e("requestRecoverInstrumentation system instrumentation is null");
                return;
            }

            if (original == ApplicationInstrumentation.this) {
                final Instrumentation base = mBase;
                Reflect.from("android.app.ActivityThread")
                        .filed("mInstrumentation", Instrumentation.class)
                        .resultAction(new RefAction<Instrumentation>() {
                            @Override
                            public void onSuccess() {
                                setBase(null);
                                if (base.getClass().getName().startsWith("com.yly")) {
                                    Reflect.from(base.getClass())
                                            .method("onInstrumentationRecovered")
                                            .invoke(base);
                                }
                            }
                        })
                        .set(activityThread, base);
            }
        }
    }

    @Override
    public void onInstrumentationReplaced(Instrumentation newInstrumentation) {
        Logger.d("ApplicationInstrumentation onInstrumentationReplaced");
    }

    @Override
    public void onInstrumentationRecovered() {
        if (mIsRequestRecoverInstrumentation) {
            if (!mRecoverHandler.hasRecoverMessage()) {
                requestRecoverInstrumentation();
            }
        }
    }

    private Instrumentation getSystemInstrumentation() {
        Object activityThread = getActivityThread();
        if (activityThread == null) {
            return null;
        }
        return Reflect.from("android.app.ActivityThread")
                .filed("mInstrumentation", Instrumentation.class)
                .get(activityThread);
    }

    private Object getActivityThread() {
        if (mActivityThread != null) return mActivityThread;
        mActivityThread = Reflect.from("android.app.ActivityThread")
                .method("currentActivityThread")
                .invoke(null);
        return mActivityThread;
    }

    public void requestReplaceInstrumentationPersist() {
        mReplacePersistTimes.incrementAndGet();
        requestReplaceInstrumentation();
    }

    public void requestRecoverInstrumentationPersist() {
        int replacePersistTimes = mReplacePersistTimes.get();
        if (replacePersistTimes > 0) {
            replacePersistTimes = mReplacePersistTimes.decrementAndGet();
        }
        if (replacePersistTimes == 0) {
            requestRecoverInstrumentation();
        }
    }

    private static class RecoverHandler extends Handler {

        private static final long RECOVER_DELAYED = 5 * 1000;

        private static final int MSG_RECOVER = 1;

        RecoverHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RECOVER: {
                    ApplicationInstrumentation.getInstance().requestRecoverInstrumentation();
                    break;
                }
            }
        }

        boolean hasRecoverMessage() {
            return hasMessages(MSG_RECOVER);
        }

        void postRecoverMessage() {
            removeRecoverMessage();
            sendEmptyMessageDelayed(MSG_RECOVER, RECOVER_DELAYED);
        }

        void removeRecoverMessage() {
            removeMessages(MSG_RECOVER);
        }
    }
}