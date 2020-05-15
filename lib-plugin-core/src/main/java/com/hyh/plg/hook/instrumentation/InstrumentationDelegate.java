package com.hyh.plg.hook.instrumentation;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.TestLooperManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * @author Administrator
 * @description
 * @data 2019/6/26
 */

class InstrumentationDelegate extends Instrumentation {

    Instrumentation mBase;

    public void setBase(Instrumentation base) {
        mBase = base;
    }

    @Override
    public void onCreate(Bundle arguments) {
        if (mBase != null) {
            mBase.onCreate(arguments);
        } else {
            super.onCreate(arguments);
        }
    }

    @Override
    public void start() {
        if (mBase != null) {
            mBase.start();
        } else {
            super.start();
        }
    }

    @Override
    public void onStart() {
        if (mBase != null) {
            mBase.onStart();
        } else {
            super.onStart();
        }
    }

    @Override
    public boolean onException(Object obj, Throwable e) {
        if (mBase != null) {
            return mBase.onException(obj, e);
        } else {
            return super.onException(obj, e);
        }
    }

    @Override
    public void sendStatus(int resultCode, Bundle results) {
        if (mBase != null) {
            mBase.sendStatus(resultCode, results);
        } else {
            super.sendStatus(resultCode, results);
        }
    }

    @Override
    public void addResults(Bundle results) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mBase != null) {
                mBase.addResults(results);
            } else {
                super.addResults(results);
            }
        } else {
            super.addResults(results);
        }
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        if (mBase != null) {
            mBase.finish(resultCode, results);
        } else {
            super.finish(resultCode, results);
        }
    }

    @Override
    public void setAutomaticPerformanceSnapshots() {
        if (mBase != null) {
            mBase.setAutomaticPerformanceSnapshots();
        } else {
            super.setAutomaticPerformanceSnapshots();
        }
    }

    @Override
    public void startPerformanceSnapshot() {
        if (mBase != null) {
            mBase.startPerformanceSnapshot();
        } else {
            super.startPerformanceSnapshot();
        }
    }

    @Override
    public void endPerformanceSnapshot() {
        if (mBase != null) {
            mBase.endPerformanceSnapshot();
        } else {
            super.endPerformanceSnapshot();
        }
    }

    @Override
    public void onDestroy() {
        if (mBase != null) {
            mBase.onDestroy();
        } else {
            super.onDestroy();
        }
    }

    @Override
    public Context getContext() {
        if (mBase != null) {
            return mBase.getContext();
        } else {
            return super.getContext();
        }
    }

    @Override
    public ComponentName getComponentName() {
        if (mBase != null) {
            return mBase.getComponentName();
        } else {
            return super.getComponentName();
        }
    }

    @Override
    public Context getTargetContext() {
        if (mBase != null) {
            return mBase.getTargetContext();
        } else {
            return super.getTargetContext();
        }
    }

    @Override
    public String getProcessName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mBase != null) {
                return mBase.getProcessName();
            } else {
                return super.getProcessName();
            }
        } else {
            return super.getProcessName();
        }
    }

    @Override
    public boolean isProfiling() {
        if (mBase != null) {
            return mBase.isProfiling();
        } else {
            return super.isProfiling();
        }
    }

    @Override
    public void startProfiling() {
        if (mBase != null) {
            mBase.startProfiling();
        } else {
            super.startProfiling();
        }
    }

    @Override
    public void stopProfiling() {
        if (mBase != null) {
            mBase.stopProfiling();
        } else {
            super.stopProfiling();
        }
    }

    @Override
    public void setInTouchMode(boolean inTouch) {
        if (mBase != null) {
            mBase.setInTouchMode(inTouch);
        } else {
            super.setInTouchMode(inTouch);
        }
    }

    @Override
    public void waitForIdle(Runnable recipient) {
        if (mBase != null) {
            mBase.waitForIdle(recipient);
        } else {
            super.waitForIdle(recipient);
        }
    }

    @Override
    public void waitForIdleSync() {
        if (mBase != null) {
            mBase.waitForIdleSync();
        } else {
            super.waitForIdleSync();
        }
    }

    @Override
    public void runOnMainSync(Runnable runner) {
        if (mBase != null) {
            mBase.runOnMainSync(runner);
        } else {
            super.runOnMainSync(runner);
        }
    }

    @Override
    public Activity startActivitySync(Intent intent) {
        if (mBase != null) {
            return mBase.startActivitySync(intent);
        } else {
            return super.startActivitySync(intent);
        }
    }

    @Override
    public void addMonitor(ActivityMonitor monitor) {
        if (mBase != null) {
            mBase.addMonitor(monitor);
        } else {
            super.addMonitor(monitor);
        }
    }

    @Override
    public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        if (mBase != null) {
            return mBase.addMonitor(filter, result, block);
        } else {
            return super.addMonitor(filter, result, block);
        }
    }

    @Override
    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        if (mBase != null) {
            return mBase.addMonitor(cls, result, block);
        } else {
            return super.addMonitor(cls, result, block);
        }
    }

    @Override
    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        if (mBase != null) {
            return mBase.checkMonitorHit(monitor, minHits);
        } else {
            return super.checkMonitorHit(monitor, minHits);
        }
    }

    @Override
    public Activity waitForMonitor(ActivityMonitor monitor) {
        if (mBase != null) {
            return mBase.waitForMonitor(monitor);
        } else {
            return super.waitForMonitor(monitor);
        }
    }

    @Override
    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        if (mBase != null) {
            return mBase.waitForMonitorWithTimeout(monitor, timeOut);
        } else {
            return super.waitForMonitorWithTimeout(monitor, timeOut);
        }
    }

    @Override
    public void removeMonitor(ActivityMonitor monitor) {
        if (mBase != null) {
            mBase.removeMonitor(monitor);
        } else {
            super.removeMonitor(monitor);
        }
    }

    @Override
    public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
        if (mBase != null) {
            return mBase.invokeMenuActionSync(targetActivity, id, flag);
        } else {
            return super.invokeMenuActionSync(targetActivity, id, flag);
        }
    }

    @Override
    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        if (mBase != null) {
            return mBase.invokeContextMenuAction(targetActivity, id, flag);
        } else {
            return super.invokeContextMenuAction(targetActivity, id, flag);
        }
    }

    @Override
    public void sendStringSync(String text) {
        if (mBase != null) {
            mBase.sendStringSync(text);
        } else {
            super.sendStringSync(text);
        }
    }

    @Override
    public void sendKeySync(KeyEvent event) {
        if (mBase != null) {
            mBase.sendKeySync(event);
        } else {
            super.sendKeySync(event);
        }
    }

    @Override
    public void sendKeyDownUpSync(int key) {
        if (mBase != null) {
            mBase.sendKeyDownUpSync(key);
        } else {
            super.sendKeyDownUpSync(key);
        }
    }

    @Override
    public void sendCharacterSync(int keyCode) {
        if (mBase != null) {
            mBase.sendCharacterSync(keyCode);
        } else {
            super.sendCharacterSync(keyCode);
        }
    }

    @Override
    public void sendPointerSync(MotionEvent event) {
        if (mBase != null) {
            mBase.sendPointerSync(event);
        } else {
            super.sendPointerSync(event);
        }
    }

    @Override
    public void sendTrackballEventSync(MotionEvent event) {
        if (mBase != null) {
            mBase.sendTrackballEventSync(event);
        } else {
            super.sendTrackballEventSync(event);
        }
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (mBase != null) {
            return mBase.newApplication(cl, className, context);
        } else {
            return super.newApplication(cl, className, context);
        }
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        if (mBase != null) {
            mBase.callApplicationOnCreate(app);
        } else {
            super.callApplicationOnCreate(app);
        }
    }

    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application, Intent intent, ActivityInfo info, CharSequence title, Activity parent, String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        if (mBase != null) {
            return mBase.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
        } else {
            return super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
        }
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (mBase != null) {
            return mBase.newActivity(cl, className, intent);
        } else {
            return super.newActivity(cl, className, intent);
        }
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if (mBase != null) {
            mBase.callActivityOnCreate(activity, icicle);
        } else {
            super.callActivityOnCreate(activity, icicle);
        }
    }

    /*@Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBase != null) {
                mBase.callActivityOnCreate(activity, icicle, persistentState);
            } else {
                super.callActivityOnCreate(activity, icicle, persistentState);
            }
        } else {
            super.callActivityOnCreate(activity, icicle, persistentState);
        }
    }*/

    @Override
    public void callActivityOnDestroy(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnDestroy(activity);
        } else {
            super.callActivityOnDestroy(activity);
        }
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        if (mBase != null) {
            mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState);
        } else {
            super.callActivityOnRestoreInstanceState(activity, savedInstanceState);
        }
    }

    /*@Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBase != null) {
                mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
            } else {
                super.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
            }
        } else {
            super.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
        }
    }*/

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        if (mBase != null) {
            mBase.callActivityOnPostCreate(activity, icicle);
        } else {
            super.callActivityOnPostCreate(activity, icicle);
        }
    }

    /*@Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBase != null) {
                mBase.callActivityOnPostCreate(activity, icicle, persistentState);
            } else {
                super.callActivityOnPostCreate(activity, icicle, persistentState);
            }
        } else {
            super.callActivityOnPostCreate(activity, icicle, persistentState);
        }
    }*/

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        if (mBase != null) {
            mBase.callActivityOnNewIntent(activity, intent);
        } else {
            super.callActivityOnNewIntent(activity, intent);
        }
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnStart(activity);
        } else {
            super.callActivityOnStart(activity);
        }
    }

    @Override
    public void callActivityOnRestart(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnRestart(activity);
        } else {
            super.callActivityOnRestart(activity);
        }
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnResume(activity);
        } else {
            super.callActivityOnResume(activity);
        }
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnStop(activity);
        } else {
            super.callActivityOnStop(activity);
        }
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        if (mBase != null) {
            mBase.callActivityOnSaveInstanceState(activity, outState);
        } else {
            super.callActivityOnSaveInstanceState(activity, outState);
        }
    }

    /*@Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState, PersistableBundle outPersistentState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBase != null) {
                mBase.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
            } else {
                super.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
            }
        } else {
            super.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
        }
    }*/

    @Override
    public void callActivityOnPause(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnPause(activity);
        } else {
            super.callActivityOnPause(activity);
        }
    }

    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        if (mBase != null) {
            mBase.callActivityOnUserLeaving(activity);
        } else {
            super.callActivityOnUserLeaving(activity);
        }
    }

    @Override
    public void startAllocCounting() {
        if (mBase != null) {
            mBase.startAllocCounting();
        } else {
            super.startAllocCounting();
        }
    }

    @Override
    public void stopAllocCounting() {
        if (mBase != null) {
            mBase.stopAllocCounting();
        } else {
            super.stopAllocCounting();
        }
    }

    @Override
    public Bundle getAllocCounts() {
        if (mBase != null) {
            return mBase.getAllocCounts();
        } else {
            return super.getAllocCounts();
        }
    }

    @Override
    public Bundle getBinderCounts() {
        if (mBase != null) {
            return mBase.getBinderCounts();
        } else {
            return super.getBinderCounts();
        }
    }

    @Override
    public UiAutomation getUiAutomation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mBase != null) {
                return mBase.getUiAutomation();
            } else {
                return super.getUiAutomation();
            }
        } else {
            return super.getUiAutomation();
        }
    }

    @Override
    public UiAutomation getUiAutomation(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mBase != null) {
                return mBase.getUiAutomation(flags);
            } else {
                return super.getUiAutomation(flags);
            }
        } else {
            return super.getUiAutomation(flags);
        }
    }

    @Override
    public TestLooperManager acquireLooperManager(Looper looper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mBase != null) {
                return mBase.acquireLooperManager(looper);
            } else {
                return super.acquireLooperManager(looper);
            }
        } else {
            return super.acquireLooperManager(looper);
        }
    }
}
