package com.hyh.plg.activity;


import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hyh.plg.activity.transform.TransformIntent;
import com.hyh.plg.activity.transform.Transformation;
import com.hyh.plg.android.BlockApplication;
import com.hyh.plg.android.BlockEnv;
import com.hyh.plg.hook.instrumentation.ActivityInstrumentation;
import com.hyh.plg.hook.instrumentation.ApplicationInstrumentation;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by tangdongwei on 2018/5/18.
 */

public class ActivityProxyImpl {

    public static final String EXTRA_BLOCK_CLASS_LOADER_HASH_CODE_INT = "extra_block_class_loader_hash_code_int";
    public static final String EXTRA_PROXY_IMPL_CLASS_PATH = "extra_proxy_impl_class_path";
    public static final String EXTRA_BLOCK_ACTIVITY_CLASS_PATH = "extra_block_activity_class_path";
    public static final String EXTRA_KEY_OF_INTENT = "extra_key_of_intent";
    public static final String EXTRA_LAUNCH_MODE = "extra_launch_mode";

    /*
    配置参数：代理Activity的包名路径
     */
    public static final String CONFIG_ACTIVITY_PROXY_CLASS_PATH = "activity_proxy_class_path";
    /*
    配置参数：SingleInstance启动模式的代理Activity的包名路径
     */
    public static final String CONFIG_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH = "activity_single_instance_proxy_class_path";
    public static final String KEY_OF_DEFAULT_ACTIVITY_PROXY_CLASS_PATH = "default_activity_proxy_class_path";
    public static final String KEY_OF_DEFAULT_SINGLE_INSTANCE_ACTIVITY_PROXY_CLASS_PATH = "default_activity_single_instance_proxy_class_path";

    public static final String DEFAULT_SINGLE_INSTALL_PROXY_ACTIVITY = "com.yly.mob.activity.ActivityProxy";
    public static final String DEFAULT_PROXY_ACTIVITY = "com.yly.mob.activity.AppActivity";

    public static boolean isActivityProxyKey(String proxyKey) {
        if (!TextUtils.isEmpty(proxyKey) && proxyKey.startsWith("ActivityProxy")) {
            return true;
        } else {
            return false;
        }
    }

    public static Object scheduleHandleActivityProxy(Context blockContext, ClassLoader hostClassLoader, String proxyKey, Map params, Object... masterClients) {
        Logger.v("------IN ActivityProxyImpl, scheduleHandleActivityProxy, proxyKey=" + proxyKey);
        switch (proxyKey) {
            case ActivityProxyKey.ATTACH: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                if (ActivityCachePool.getInstance().containsKey(hashCode)) {
                    ActivityCachePool.getInstance().remove(hashCode);
                }
                IActivity activityProxyImpl = ActivityProxyImpl.getActivityProxyImpl(activity, masterClients);
                if (activityProxyImpl != null) {
                    ActivityProxyImpl.fixInnerActivityContext(activity);
                    ActivityProxyImpl.fixActivityIntent(activity);
                    ActivityCachePool.getInstance().put(hashCode, activityProxyImpl);
                    activityProxyImpl.attach(activity);
                    return activityProxyImpl;
                }
                return null;
            }
            case ActivityProxyKey.ON_BEFORE_CREATE: {
                Activity activity = (Activity) masterClients[0];
                Bundle bundle = masterClients[1] == null ? null : (Bundle) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onBeforeCreate(bundle);
                }
                return null;
            }
            case ActivityProxyKey.ON_AFTER_CREATE: {
                Activity activity = (Activity) masterClients[0];
                Bundle bundle = masterClients[1] == null ? null : (Bundle) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onAfterCreate(bundle);
                }
                return null;
            }
            case ActivityProxyKey.ON_START: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onStart();
                }
                return null;
            }
            case ActivityProxyKey.ON_RESTART: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onRestart();
                }
                return null;
            }
            case ActivityProxyKey.ON_RESUME: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onResume();
                }
                return null;
            }
            case ActivityProxyKey.ON_NEW_INTENT: {
                Activity activity = (Activity) masterClients[0];
                Intent intent = masterClients[1] == null ? null : (Intent) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onNewIntent(intent);
                }
                return null;
            }
            case ActivityProxyKey.ON_SAVE_INSTANCE_STATE_1: {
                Activity activity = (Activity) masterClients[0];
                Bundle bundle = masterClients[1] == null ? null : (Bundle) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onSaveInstanceState(bundle);
                }
                return null;
            }
//            case ActivityProxyKey.ON_SAVE_INSTANCE_STATE_2: {
//                Activity activity = (Activity) masterClients[0];
//                Bundle bundle = masterClients[1] == null ? null : (Bundle) masterClients[1];
//                PersistableBundle persistableBundle = masterClients[2] == null ? null : (PersistableBundle) masterClients[2];
//                Integer hashCode = activity.hashCode();
//                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
//                if (activityProxyImpl != null) {
//                    activityProxyImpl.onSaveInstanceState(bundle, persistableBundle);
//                }
//                return null;
//            }
            case ActivityProxyKey.ON_PAUSE: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onPause();
                }
                return null;
            }
            case ActivityProxyKey.ON_STOP: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onStop();
                }
                return null;
            }
            case ActivityProxyKey.ON_DESTROY: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onDestroy();
                    ActivityCachePool.getInstance().remove(hashCode);
                }
                return null;
            }
            case ActivityProxyKey.ON_CONFIGURATION_CHANGED: {
                Activity activity = (Activity) masterClients[0];
                Configuration configuration = masterClients[1] == null ? null : (Configuration) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onConfigurationChanged(configuration);
                }
                return null;
            }
            case ActivityProxyKey.ON_LOW_MEMORY: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onLowMemory();
                }
                return null;
            }
            case ActivityProxyKey.ON_TRIM_MEMORY: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onTrimMemory((int) masterClients[1]);
                }
                return null;
            }
            case ActivityProxyKey.ON_ATTACH_FRAGMENT: {
                Activity activity = (Activity) masterClients[0];
                Fragment fragment = masterClients[1] == null ? null : (Fragment) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onAttachFragment(fragment);
                }
                return null;
            }
            case ActivityProxyKey.ON_ACTIVITY_RESULT: {
                Activity activity = (Activity) masterClients[0];
                Intent intent = masterClients[3] == null ? null : (Intent) masterClients[3];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onActivityResult((int) masterClients[1], (int) masterClients[2], intent);
                }
                return null;
            }
            case ActivityProxyKey.ON_RESTORE_INSTANCE_STATE: {
                Activity activity = (Activity) masterClients[0];
                Bundle bundle = masterClients[1] == null ? null : (Bundle) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onRestoreInstanceState(bundle);
                }
                return null;
            }
            case ActivityProxyKey.ON_USER_LEAVE_HINT: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onUserLeaveHint();
                }
                return null;
            }
            case ActivityProxyKey.ON_USER_INTERACTION: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onUserInteraction();
                }
                return null;
            }
            case ActivityProxyKey.ON_BACK_PRESSED: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onBackPressed();
                }
                return null;
            }
            case ActivityProxyKey.ON_TOUCH_EVENT: {
                Activity activity = (Activity) masterClients[0];
                MotionEvent motionEvent = masterClients[1] == null ? null : (MotionEvent) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onTouchEvent(motionEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.ON_KEY_UP: {
                Activity activity = (Activity) masterClients[0];
                KeyEvent keyEvent = masterClients[2] == null ? null : (KeyEvent) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onKeyUp((int) masterClients[1], keyEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.ON_WINDOW_ATTRIBUTES_CHANGED: {
                Activity activity = (Activity) masterClients[0];
                WindowManager.LayoutParams layoutParams = masterClients[1] == null ? null : (WindowManager.LayoutParams) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onWindowAttributesChanged(layoutParams);
                }
                return null;
            }
            case ActivityProxyKey.ON_WINDOW_FOCUS_CHANGED: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onWindowFocusChanged((boolean) masterClients[1]);
                }
                return null;
            }
            case ActivityProxyKey.ON_CREATE_OPTIONS_MENU: {
                Activity activity = (Activity) masterClients[0];
                Menu menu = masterClients[1] == null ? null : (Menu) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreateOptionsMenu(menu);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.ON_OPTIONS_ITEM_SELECTED: {
                Activity activity = (Activity) masterClients[0];
                MenuItem menuItem = masterClients[1] == null ? null : (MenuItem) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onOptionsItemSelected(menuItem);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.ON_KEY_DOWN: {
                Activity activity = (Activity) masterClients[0];
                KeyEvent keyEvent = masterClients[2] == null ? null : (KeyEvent) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onKeyDown((int) masterClients[1], keyEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.ON_KEY_LONG_PRESS: {
                Activity activity = (Activity) masterClients[0];
                KeyEvent keyEvent = masterClients[2] == null ? null : (KeyEvent) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onKeyLongPress((int) masterClients[1], keyEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.DISPATCH_KEY_EVENT: {
                Activity activity = (Activity) masterClients[0];
                KeyEvent keyEvent = masterClients[1] == null ? null : (KeyEvent) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.dispatchKeyEvent(keyEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.GET_APPLICATION_CONTEXT: {
                return blockContext;
            }
            case ActivityProxyKey.START_ACTIVITY_FOR_RESULT: {
                Activity activity = (Activity) masterClients[0];
                Intent intent = masterClients[1] == null ? null : (Intent) masterClients[1];
                int requestCode = (int) masterClients[2];
                Bundle options = (Bundle) masterClients[3];
                TransformIntent transformIntent = transformIntentForActivityProxy(intent);
                if (transformIntent == null) {
                    intent = intent == null ? null : intent.putExtra(EXTRA_KEY_OF_INTENT, "1");
                    activity.startActivityForResult(intent, requestCode, options);
                } else {
                    if (transformIntent.transformIntent == null) {
                        return null;
                    }
                    /*HookInstaller hookInstaller = new HookInstaller();
                    hookInstaller.install();*/
                    ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();

                    activity.startActivityForResult(transformIntent.transformIntent, requestCode, options);
                }
                return null;
            }
            case ActivityProxyKey.GET_FRAGMENT_MANAGER: {
//                Activity activity = (Activity) masterClients[0];
                String result = "super";
                return result;
            }
            case ActivityProxyKey.dispatchTouchEvent: {
                Activity activity = (Activity) masterClients[0];
                MotionEvent motionEvent = (MotionEvent) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.dispatchTouchEvent(motionEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.dispatchTrackballEvent: {
                Activity activity = (Activity) masterClients[0];
                MotionEvent motionEvent = (MotionEvent) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.dispatchTrackballEvent(motionEvent);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onApplyThemeResource: {
                Activity activity = (Activity) masterClients[0];
                Resources.Theme theme = (Resources.Theme) masterClients[1];
                int resid = (int) masterClients[2];
                boolean first = (boolean) masterClients[3];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onApplyThemeResource(theme, resid, first);
                }
                return null;
            }
            case ActivityProxyKey.onChildTitleChanged: {
                Activity activity = (Activity) masterClients[0];
                Activity childActivity = (Activity) masterClients[1];
                CharSequence title = (CharSequence) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onChildTitleChanged(childActivity, title);
                }
                return null;
            }
            case ActivityProxyKey.onContentChanged: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onContentChanged();
                }
                return null;
            }
            case ActivityProxyKey.onContextItemSelected: {
                Activity activity = (Activity) masterClients[0];
                MenuItem item = (MenuItem) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onContextItemSelected(item);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onContextMenuClosed: {
                Activity activity = (Activity) masterClients[0];
                Menu menu = (Menu) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onContextMenuClosed(menu);
                }
                return null;
            }
            case ActivityProxyKey.onCreateContextMenu: {
                Activity activity = (Activity) masterClients[0];
                ContextMenu menu = (ContextMenu) masterClients[1];
                View v = (View) masterClients[2];
                ContextMenu.ContextMenuInfo menuInfo = (ContextMenu.ContextMenuInfo) masterClients[3];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onCreateContextMenu(menu, v, menuInfo);
                }
                return null;
            }
            case ActivityProxyKey.onCreateDescription: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreateDescription();
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onCreateDialog: {
                Activity activity = (Activity) masterClients[0];
                int id = (int) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreateDialog(id);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onCreatePanelMenu: {
                Activity activity = (Activity) masterClients[0];
                int featureId = (int) masterClients[1];
                Menu menu = (Menu) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreatePanelMenu(featureId, menu);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onCreatePanelView: {
                Activity activity = (Activity) masterClients[0];
                int featureId = (int) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreatePanelView(featureId);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onCreateThumbnail: {
                Activity activity = (Activity) masterClients[0];
                Bitmap outBitmap = (Bitmap) masterClients[1];
                Canvas canvas = (Canvas) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreateThumbnail(outBitmap, canvas);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onCreateView: {
                Activity activity = (Activity) masterClients[0];
                String name = (String) masterClients[1];
                Context context = (Context) masterClients[2];
                AttributeSet attrs = (AttributeSet) masterClients[3];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onCreateView(name, context, attrs);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onKeyMultiple: {
                Activity activity = (Activity) masterClients[0];
                int keyCode = (int) masterClients[1];
                int repeatCount = (int) masterClients[2];
                KeyEvent event = (KeyEvent) masterClients[3];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onKeyMultiple(keyCode, repeatCount, event);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onMenuItemSelected: {
                Activity activity = (Activity) masterClients[0];
                int featureId = (int) masterClients[1];
                MenuItem item = (MenuItem) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onMenuItemSelected(featureId, item);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onMenuOpened: {
                Activity activity = (Activity) masterClients[0];
                int featureId = (int) masterClients[1];
                Menu menu = (Menu) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onMenuOpened(featureId, menu);
                } else {
                    return null;
                }
            }
            case ActivityProxyKey.onOptionsMenuClosed: {
                Activity activity = (Activity) masterClients[0];
                Menu menu = (Menu) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onOptionsMenuClosed(menu);
                }
                return null;
            }
            case ActivityProxyKey.onPanelClosed: {
                Activity activity = (Activity) masterClients[0];
                int featureId = (int) masterClients[1];
                Menu menu = (Menu) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onPanelClosed(featureId, menu);
                }
                return null;
            }
            case ActivityProxyKey.onPostCreate: {
                Activity activity = (Activity) masterClients[0];
                Bundle savedInstanceState = (Bundle) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onPostCreate(savedInstanceState);
                }
                return null;
            }
            case ActivityProxyKey.onPostResume: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onPostResume();
                }
                return null;
            }
            case ActivityProxyKey.onPrepareDialog: {
                Activity activity = (Activity) masterClients[0];
                int id = (int) masterClients[1];
                Dialog dialog = (Dialog) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onPrepareDialog(id, dialog);
                }
                return null;
            }
            case ActivityProxyKey.onPrepareOptionsMenu: {
                Activity activity = (Activity) masterClients[0];
                Menu menu = (Menu) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onPrepareOptionsMenu(menu);
                }
                return null;
            }
            case ActivityProxyKey.onPreparePanel: {
                Activity activity = (Activity) masterClients[0];
                int featureId = (int) masterClients[1];
                View view = (View) masterClients[2];
                Menu menu = (Menu) masterClients[3];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onPreparePanel(featureId, view, menu);
                }
                return null;
            }
            case ActivityProxyKey.onRetainNonConfigurationInstance: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onRetainNonConfigurationInstance();
                }
                return null;
            }
            case ActivityProxyKey.onSearchRequested: {
                Activity activity = (Activity) masterClients[0];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onSearchRequested();
                }
                return null;
            }
            case ActivityProxyKey.onTitleChanged: {
                Activity activity = (Activity) masterClients[0];
                CharSequence title = (CharSequence) masterClients[1];
                int color = (int) masterClients[2];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    activityProxyImpl.onTitleChanged(title, color);
                }
                return null;
            }
            case ActivityProxyKey.onTrackballEvent: {
                Activity activity = (Activity) masterClients[0];
                MotionEvent event = (MotionEvent) masterClients[1];
                Integer hashCode = activity.hashCode();
                IActivity activityProxyImpl = ActivityCachePool.getInstance().get(hashCode);
                if (activityProxyImpl != null) {
                    return activityProxyImpl.onTrackballEvent(event);
                }
                return null;
            }
            case ActivityProxyKey.getPackageManager: {
                return blockContext.getPackageManager();
            }
            default: {
                return null;
            }
        }
    }

    public static TransformIntent transformIntentForActivityProxy(Intent intent) {
        Intent originalIntent = intent;
        {
            List<Transformation> transformationList = BlockEnv.sBeginTransformationList;
            for (Transformation transformation : transformationList) {
                TransformIntent beginTransformIntent = transformation.transform(intent);
                if (beginTransformIntent != null) {
                    if (beginTransformIntent.transformIntent == null) {
                        return beginTransformIntent;
                    } else {
                        intent = beginTransformIntent.transformIntent;
                    }
                    break;
                }
            }
        }
        {
            Transformation proxyActivityTransformation = BlockEnv.sProxyActivityTransformation;
            TransformIntent transformIntent = proxyActivityTransformation.transform(intent);
            if (transformIntent != null) {
                if (transformIntent.transformIntent == null) {
                    return transformIntent;
                } else {
                    intent = transformIntent.transformIntent;
                }
            }
        }
        {
            List<Transformation> transformationList = BlockEnv.sMidTransformationList;
            for (Transformation transformation : transformationList) {
                TransformIntent midTransformIntent = transformation.transform(intent);
                if (midTransformIntent != null) {
                    if (midTransformIntent.transformIntent == null) {
                        return midTransformIntent;
                    } else {
                        intent = midTransformIntent.transformIntent;
                    }
                    break;
                }
            }
        }
        {
            Transformation endTransformation = BlockEnv.sEndTransformation;
            if (endTransformation != null) {
                TransformIntent endTransformIntent = endTransformation.transform(intent);
                if (endTransformIntent != null) {
                    return endTransformIntent;
                }
            }
        }
        return new TransformIntent(originalIntent, intent);
    }

    public static IActivity getActivityProxyImpl(Activity activity, Object... masterClients) {
        try {
            //宿主的activity对象
            Intent intent = activity.getIntent();
            if (intent == null) {
                Logger.w("IN ActivityProxyImpl, performHandleActivityProxy(), intent == null, return null");
                return null;
            }
            //构造插件中用于接收activity生命周期的类
            String proxyImplClassPath = intent.getStringExtra(EXTRA_PROXY_IMPL_CLASS_PATH);
            if (TextUtils.isEmpty(proxyImplClassPath)) {
                Logger.w("IN ActivityProxyImpl, performHandleActivityProxy(), TextUtils.isEmpty(proxyImplClassPath), return null");
                return null;
            }
            IActivity activityProxyImpl = (IActivity) ActivityProxyImpl.class.getClassLoader().loadClass(proxyImplClassPath).newInstance();
            return activityProxyImpl;
        } catch (Exception e) {
            Logger.e("IN ActivityProxyImpl, getActivityProxyImpl ERROR : " + e.toString());
        }
        return null;
    }

    /**
     * 修复activity的上下文
     *
     * @param activity
     */
    public static void fixInnerActivityContext(Activity activity) {
        if (activity == null) {
            return;
        }
        Context baseContext = getBlockActivityBaseContext(activity);
        try {
            Field mApplicationField = Service.class.getDeclaredField("mApplication");
            mApplicationField.setAccessible(true);
            mApplicationField.set(activity, baseContext);
        } catch (Exception e) {
            Logger.e("IN BlockServiceServer, fixServiceContext(), fix Application ERROR : " + e.toString());
        }
        try {
            Field mTheme = ContextThemeWrapper.class.getDeclaredField("mTheme");
            mTheme.setAccessible(true);
            mTheme.set(activity, baseContext.getTheme());
        } catch (Exception e) {
            Logger.d("IN FrameCustomerProxy, fixInnerActivityContext() mTheme, not found");
        }
        try {
            Field mInflater = ContextThemeWrapper.class.getDeclaredField("mInflater");
            mInflater.setAccessible(true);
            mInflater.set(activity, baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        } catch (Exception e) {
            Logger.d("IN FrameCustomerProxy, fixInnerActivityContext() mInflater, not found");
        }
        try {
            Field mResources = ContextThemeWrapper.class.getDeclaredField("mResources");
            mResources.setAccessible(true);
            mResources.set(activity, baseContext.getResources());
        } catch (Exception e) {
            Logger.d("IN FrameCustomerProxy, fixInnerActivityContext() mResources, not found");
        }
        try {
            Field mThemeResource = ContextThemeWrapper.class.getDeclaredField("mThemeResource");
            mThemeResource.setAccessible(true);
            mThemeResource.set(activity, BlockApplication.DEFAULT_THEME_RESOURCE);
        } catch (Exception e) {
            Logger.d("IN FrameCustomerProxy, fixInnerActivityContext() mThemeResource, not found");
        }
        try {
            Field mBase = ContextWrapper.class.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(activity, baseContext);
        } catch (Exception e) {
            Logger.d("IN FrameCustomerProxy, fixInnerActivityContext() ContextWrapper's mBase, not found");
        }
        try {
            Field mBase = ContextThemeWrapper.class.getDeclaredField("mBase");
            mBase.setAccessible(true);
            mBase.set(activity, baseContext);
        } catch (Exception e) {
            Logger.d("IN FrameCustomerProxy, fixInnerActivityContext() ContextThemeWrapper's mBase, not found");
        }
    }


    /**
     * 修复activity的上下文
     *
     * @param activity
     */
    public static void fixBlockActivityContext(Activity activity) {
        if (activity == null) {
            return;
        }
        Context baseContext = getBlockActivityBaseContext(activity);

        Reflect.from(Activity.class).filed("mApplication").set(activity, baseContext);

        Reflect.from(ContextWrapper.class).filed("mBase").set(activity, baseContext);
        Reflect.from(ContextThemeWrapper.class).filed("mBase").set(activity, baseContext);
        Reflect.from(Activity.class).filed("mBase").set(activity, baseContext);

        Reflect.from(Activity.class).filed("mInflater").set(activity, LayoutInflater.from(baseContext).cloneInContext(activity));
        Reflect.from(Activity.class).filed("mTheme").set(activity, null);
        Reflect.from(Activity.class).filed("mThemeResource").set(activity, BlockApplication.DEFAULT_THEME_RESOURCE);
        Reflect.from(Activity.class).filed("mResources").set(activity, null);

        /*Reflect.from(Activity.class).filed("mResources").set(activity,
                new BlockResources(blockResources.getAssets(), activityResources.getDisplayMetrics(), activityResources.getConfiguration()));*/
    }

    public static void fixActivityWindow(Activity activity) {
        Window window = activity.getWindow();
        Reflect.from(window.getClass())
                .filed("mLayoutInflater")
                .set(window, activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    /**
     * 修复activity修改的intent
     *
     * @param activity
     */
    public static void fixActivityIntent(Activity activity) {
        if (activity == null) {
            return;
        }
        try {
            Intent proxyIntent = activity.getIntent();
            if (proxyIntent == null) {
                return;
            }
            String data = proxyIntent.getDataString();

            IntentDataCarrier intentDataCarrier = IntentDataCarrier.create(data);
            if (intentDataCarrier == null) {
                Logger.w("IN ActivityProxyImpl, fixActivityIntent() ERROR : intentDataCarrier == null");
                return;
            }

            String key = intentDataCarrier.getKeyOfIntent();
            if (TextUtils.isEmpty(key)) {
                return;
            }
            String proxyImplClassPath = intentDataCarrier.getProxyImplClassPath();
            int launchMode = intentDataCarrier.getLaunchMode();
            Intent realIntent = IntentCachePool.getInstance().get(key);
            if (realIntent == null) {
                return;
            }

            //tangdongwei 20190305 为反序列化准备classloader
            proxyIntent.setExtrasClassLoader(ActivityProxyImpl.class.getClassLoader());
            realIntent.setExtrasClassLoader(ActivityProxyImpl.class.getClassLoader());
            //tangdongwei 20190305 为反序列化准备classloader
            //反序列化
            realIntent.putExtras(proxyIntent);

            //把manifest中预埋的Activity路径放置
            ComponentName component = proxyIntent.getComponent();
            if (component != null) {
                String className = component.getClassName();
                if (!TextUtils.isEmpty(className)) {
                    realIntent.putExtra(IntentExtras.MANIFEST_STUB_ACTIVITY, className);
                }
            }

            //把请求打开预埋activity的intent里的这些字段取出来，再塞进原始的intent里，再替换activity里的intent
            //因为这些关键数据本来是放在intent的data中的，但是拿到真正的intent后，不能覆盖掉它的intent，那么只能
            //把这些数据放在bundle中了，同时，由于已经把intent的classloader改成插件的，此时触发序列化没有问题
            //这里是代理activity起来后的时机比较早的操作，后续操作从intent中拿数据
            realIntent.putExtra(EXTRA_KEY_OF_INTENT, key);
            realIntent.putExtra(EXTRA_PROXY_IMPL_CLASS_PATH, proxyImplClassPath);
            realIntent.putExtra(EXTRA_LAUNCH_MODE, launchMode);
            activity.setIntent(realIntent);
        } catch (Exception e) {
            Logger.w("IN ActivityProxyImpl, fixActivityIntent(), failed : " + e.toString());
        }
    }

    public static boolean isBlockActivity(Activity activity) {
        return isBlockActivity(activity.getClass().getName());
    }

    public static boolean isBlockActivity(String activityName) {


        if (Reflect.classForName(activityName) == null) return false;
        ActivityInfo[] activities = BlockEnv.sBlockPackageInfo.activities;
        if (activities != null && activities.length > 0) {
            for (ActivityInfo activity : activities) {
                if (TextUtils.equals(activity.name, activityName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void fixActivityInstrumentation(Activity activity) {
        ActivityInstrumentation.obtain(activity).requestReplaceInstrumentation();
    }

    private static Context getBlockActivityBaseContext(Activity activity) {
        Context context = BlockEnv.sBlockActivityBaseContextMap.get(activity.getClass().getName());
        if (context == null) context = BlockEnv.sBlockApplication;
        return context;
    }
}