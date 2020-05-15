package com.hyh.plg.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.yly.mob.activity.transform.TransformIntent;
import com.yly.mob.android.BlockApplication;
import com.yly.mob.android.BlockEnv;
import com.yly.mob.hook.instrumentation.ApplicationInstrumentation;
import com.yly.mob.service.BlockServiceServer;
import com.yly.mob.utils.Logger;

/**
 * Created by tangdongwei on 2018/5/14.
 */

public class ActivityProxyInner2 extends Activity {

    private IActivity activityImpl = new BaseProxyActivity();
    private boolean mToBeRecycleImmediately = false;
    private boolean mTrimMemory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Integer hashCode = hashCode();
        Logger.i("--------------ActivityProxyInner2 running----------------- : " + hashCode);

        ActivityProxyImpl.fixInnerActivityContext(this);
        //先修复intent（序列化问题）要紧，不然后面的操作一旦触发序列化会有问题
        ActivityProxyImpl.fixActivityIntent(this);

        ActivityLaunchRecord.recordActivityLaunchInfo(this);

        IActivity activityProxyImpl = ActivityProxyImpl.getActivityProxyImpl(this);
        if (activityProxyImpl != null) {
            activityProxyImpl.attach(this);
            activityImpl = activityProxyImpl;
        } else {
            activityImpl = new BaseProxyActivity();
        }
        activityImpl.onBeforeCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        activityImpl.onAfterCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        Logger.v("IN ActivityProxyInner2, onStart()");
        super.onStart();
        activityImpl.onStart();
    }

    @Override
    public void onRestart() {
        Logger.v("IN ActivityProxyInner2, onRestart()");
        super.onRestart();
        activityImpl.onRestart();
    }

    @Override
    public void onResume() {
        Logger.v("IN ActivityProxyInner2, onResume()");
        mToBeRecycleImmediately = false;
        mTrimMemory = false;
        super.onResume();
        activityImpl.onResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Logger.v("IN ActivityProxyInner2, onNewIntent(Intent intent)");
        super.onNewIntent(intent);
        activityImpl.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.v("IN ActivityProxyInner2, onSaveInstanceState(Bundle outState)");
//        super.onSaveInstanceState(outState);
        mToBeRecycleImmediately = true;
        activityImpl.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Logger.v("IN ActivityProxyInner2, onPause()");
        super.onPause();
        activityImpl.onPause();
    }

    @Override
    public void onStop() {
        Logger.v("IN ActivityProxyInner2, onStop()");
        super.onStop();
        activityImpl.onStop();
    }

    @Override
    public void onDestroy() {
        ActivityLaunchRecord.removeActivityLaunchInfo(this);
        Integer hashCode = hashCode();
        Logger.i("--------------ActivityProxyInner2 onDestroy----------------- : " + hashCode);
        super.onDestroy();
        activityImpl.onDestroy();
        activityImpl = new BaseProxyActivity();
        //finish();
        if (mToBeRecycleImmediately) {
            /*HookInstaller hookInstaller = new HookInstaller();
            hookInstaller.install();*/
            ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
            if (mTrimMemory) {
                finish();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.v("IN ActivityProxyInner2, onConfigurationChanged(Configuration newConfig)");
        super.onConfigurationChanged(newConfig);
        activityImpl.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        Logger.v("IN ActivityProxyInner2, onLowMemory()");
        super.onLowMemory();
        activityImpl.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Logger.v("IN ActivityProxyInner2, onTrimMemory(int level)");
        super.onTrimMemory(level);
        mTrimMemory = true;
        activityImpl.onTrimMemory(level);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Logger.v("IN ActivityProxyInner2, onAttachFragment(Fragment fragment)");
        super.onAttachFragment(fragment);
        activityImpl.onAttachFragment(fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.v("IN ActivityProxyInner2, onActivityResult(int requestCode, int resultCode, Intent data)");
        super.onActivityResult(requestCode, resultCode, data);
        activityImpl.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.v("IN ActivityProxyInner2, onRestoreInstanceState(Bundle savedInstanceState)");
//        super.onRestoreInstanceState(savedInstanceState);
        activityImpl.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onUserLeaveHint() {
        Logger.v("IN ActivityProxyInner2, onUserLeaveHint()");
        super.onUserLeaveHint();
        activityImpl.onUserLeaveHint();
    }

    @Override
    public void onUserInteraction() {
        Logger.v("IN ActivityProxyInner2, onUserInteraction()");
        super.onUserInteraction();
        activityImpl.onUserInteraction();
    }

    @Override
    public void onBackPressed() {
        Logger.v("IN ActivityProxyInner2, onBackPressed()");
        if (!activityImpl.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Logger.v("IN ActivityProxyInner2, onTouchEvent(MotionEvent event)");
        return activityImpl.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Logger.v("IN ActivityProxyInner2, onKeyUp(int keyCode, KeyEvent event)");
        return activityImpl.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        Logger.v("IN ActivityProxyInner2, onWindowAttributesChanged(WindowManager.LayoutParams params)");
        super.onWindowAttributesChanged(params);
        activityImpl.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Logger.v("IN ActivityProxyInner2, onWindowFocusChanged(boolean hasFocus)");
        super.onWindowFocusChanged(hasFocus);
        activityImpl.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logger.v("IN ActivityProxyInner2, onCreateOptionsMenu(Menu menu)");
        return activityImpl.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.v("IN ActivityProxyInner2, onOptionsItemSelected(MenuItem item)");
        return activityImpl.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Logger.v("IN ActivityProxyInner2, onKeyDown(int keyCode, KeyEvent event)");
        return activityImpl.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Logger.v("IN ActivityProxyInner2, onKeyLongPress(int keyCode, KeyEvent event)");
        return activityImpl.onKeyLongPress(keyCode, event) || super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Logger.v("IN ActivityProxyInner2, dispatchKeyEvent(KeyEvent event)");
        return activityImpl.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    /*
     **********
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Logger.v("IN ActivityProxyInner2, dispatchTouchEvent(MotionEvent ev)");
        return activityImpl.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        Logger.v("IN ActivityProxyInner2, dispatchTrackballEvent(MotionEvent motionEvent)");
        return activityImpl.dispatchTrackballEvent(motionEvent) || super.dispatchTrackballEvent(motionEvent);
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        Logger.v("IN ActivityProxyInner2, onApplyThemeResource(Resources.Theme theme, int resid, boolean first)");
        super.onApplyThemeResource(theme, resid, first);
        activityImpl.onApplyThemeResource(theme, resid, first);
    }

    @Override
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
        Logger.v("IN ActivityProxyInner2, onChildTitleChanged(Activity childActivity, CharSequence title)");
        super.onChildTitleChanged(childActivity, title);
        activityImpl.onChildTitleChanged(childActivity, title);
    }

    @Override
    public void onContentChanged() {
        Logger.v("IN ActivityProxyInner2, onContentChanged()");
        super.onContentChanged();
        activityImpl.onContentChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Logger.v("IN ActivityProxyInner2, onContextItemSelected(MenuItem item)");
        return activityImpl.onContextItemSelected(item) || super.onContextItemSelected(item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        Logger.v("IN ActivityProxyInner2, onContextMenuClosed(Menu menu)");
        super.onContextMenuClosed(menu);
        activityImpl.onContextMenuClosed(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Logger.v("IN ActivityProxyInner2, onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)");
        super.onCreateContextMenu(menu, v, menuInfo);
        activityImpl.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public CharSequence onCreateDescription() {
        Logger.v("IN ActivityProxyInner2, onCreateDescription()");
        CharSequence charSequence = activityImpl.onCreateDescription();
        return charSequence != null ? charSequence : super.onCreateDescription();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Logger.v("IN ActivityProxyInner2, onCreateDialog(int id)");
        Dialog dialog = activityImpl.onCreateDialog(id);
        return dialog != null ? dialog : super.onCreateDialog(id);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        Logger.v("IN ActivityProxyInner2, onCreatePanelMenu(int featureId, Menu menu)");
        return activityImpl.onCreatePanelMenu(featureId, menu) || super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        Logger.v("IN ActivityProxyInner2, onCreatePanelView(int featureId)");
        View view = activityImpl.onCreatePanelView(featureId);
        return view != null ? view : super.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        Logger.v("IN ActivityProxyInner2, onCreateThumbnail(Bitmap outBitmap, Canvas canvas)");
        return activityImpl.onCreateThumbnail(outBitmap, canvas) || super.onCreateThumbnail(outBitmap, canvas);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        Logger.v("IN ActivityProxyInner2, onCreateView(String name, Context context, AttributeSet attrs)");
        View view = activityImpl.onCreateView(name, context, attrs);
        return view != null ? view : super.onCreateView(name, context, attrs);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        Logger.v("IN ActivityProxyInner2, onKeyMultiple(int keyCode, int repeatCount, KeyEvent event)");
        return activityImpl.onKeyMultiple(keyCode, repeatCount, event) || super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Logger.v("IN ActivityProxyInner2, onMenuItemSelected(int featureId, MenuItem item)");
        return activityImpl.onMenuItemSelected(featureId, item) || super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Logger.v("IN ActivityProxyInner2, onMenuOpened(int featureId, Menu menu)");
        return activityImpl.onMenuOpened(featureId, menu) || super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        Logger.v("IN ActivityProxyInner2, onOptionsMenuClosed(Menu menu)");
        super.onOptionsMenuClosed(menu);
        activityImpl.onOptionsMenuClosed(menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        Logger.v("IN ActivityProxyInner2, onPanelClosed(int featureId, Menu menu)");
        super.onPanelClosed(featureId, menu);
        activityImpl.onPanelClosed(featureId, menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Logger.v("IN ActivityProxyInner2, onPostCreate(Bundle savedInstanceState)");
        super.onPostCreate(savedInstanceState);
        activityImpl.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        Logger.v("IN ActivityProxyInner2, onPostResume()");
        super.onPostResume();
        activityImpl.onPostResume();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        Logger.v("IN ActivityProxyInner2, onPrepareDialog(int id, Dialog dialog)");
        super.onPrepareDialog(id, dialog);
        activityImpl.onPrepareDialog(id, dialog);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Logger.v("IN ActivityProxyInner2, onPrepareOptionsMenu(Menu menu)");
        return activityImpl.onPrepareOptionsMenu(menu) || super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        Logger.v("IN ActivityProxyInner2, onPreparePanel(int featureId, View view, Menu menu)");
        return activityImpl.onPreparePanel(featureId, view, menu) || super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        Logger.v("IN ActivityProxyInner2, onRetainNonConfigurationInstance()");
        Object result = activityImpl.onRetainNonConfigurationInstance();
        return result != null ? result : super.onRetainNonConfigurationInstance();
    }

    @Override
    public boolean onSearchRequested() {
        Logger.v("IN ActivityProxyInner2, onSearchRequested()");
        return activityImpl.onSearchRequested() || super.onSearchRequested();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        Logger.v("IN ActivityProxyInner2, onTitleChanged(CharSequence title, int color)");
        super.onTitleChanged(title, color);
        activityImpl.onTitleChanged(title, color);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        Logger.v("IN ActivityProxyInner2, onTrackballEvent(MotionEvent event)");
        return activityImpl.onTrackballEvent(event) || super.onTrackballEvent(event);
    }

    @Override
    public void finish() {
        Logger.v("IN ActivityProxyInner2, finish()");
        super.finish();
        activityImpl.onFinish();
    }

    /*
     *************
     */

    @Override
    public Context getApplicationContext() {
        Context context = null;
        if (activityImpl != null) {
            context = activityImpl.onGetApplicationContext();
        }
        context = (context == null ? BlockApplication.blockApplicationContext : context);
        Logger.v("IN ActivityProxyInner2, getApplicationContext(), context=" + context);
        return context;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        Logger.v("IN ActivityProxyInner2, startActivityForResult(Intent intent, int requestCode, Bundle options)");
        //startActivityForResult事件先传递给插件，如果插件不消耗，这里再执行；如果插件消耗，则不再执行
        if (!activityImpl.onStartActivityForResult(intent, requestCode, options)) {
            TransformIntent transformIntent = ActivityProxyImpl.transformIntentForActivityProxy(intent);
            if (transformIntent == null) {
                super.startActivityForResult(intent, requestCode, options);
            } else {
                if (transformIntent.transformIntent == null) {
                    return;
                }
                /*HookInstaller hookInstaller = new HookInstaller();
                hookInstaller.install();*/
                ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
                super.startActivityForResult(transformIntent.transformIntent, requestCode, options);
            }
        }
    }

    @Override
    public void setTheme(int resid) {
        if (activityImpl != null && activityImpl.setTheme(resid)) return;
        super.setTheme(resid);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (activityImpl != null && activityImpl.setRequestedOrientation(requestedOrientation)) return;
        super.setRequestedOrientation(requestedOrientation);
    }

    @Override
    public PackageManager getPackageManager() {
        Logger.v("IN ActivityProxyInner2, getPackageManager()");
        return BlockApplication.blockApplicationContext.getPackageManager();
    }

    @SuppressWarnings("all")
    @Override
    public LayoutInflater getLayoutInflater() {
        return BlockApplication.blockApplication.getLayoutInflater();
    }

    /*
     ********************************** 处理service BEGIN ********************************
     */
    @Override
    public ComponentName startService(Intent service) {
        if (!BlockEnv.isServiceProxyEnable()) {
            return super.startService(service);
        }
        ComponentName componentName = BlockServiceServer.getInstance(getApplicationContext()).startService(service);
        return componentName == null ? super.startService(service) : componentName;
    }

    @Override
    public ComponentName startForegroundService(Intent service) {
        if (!BlockEnv.isServiceProxyEnable()) {
            return super.startForegroundService(service);
        }
        ComponentName componentName = BlockServiceServer.getInstance(getApplicationContext()).startForegroundService(service);
        return componentName == null ? super.startForegroundService(service) : componentName;
    }

    @Override
    public boolean stopService(Intent name) {
        if (!BlockEnv.isServiceProxyEnable()) {
            return super.stopService(name);
        }
        return BlockServiceServer.getInstance(getApplicationContext()).stopService(name) || super.stopService(name);
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        if (!BlockEnv.isServiceProxyEnable()) {
            return super.bindService(service, conn, flags);
        }
        return BlockServiceServer.getInstance(getApplicationContext()).bindService(service, conn, flags) || super.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        if (BlockServiceServer.getInstance(getApplicationContext()).unbindService(conn)) return;
        try {
            super.unbindService(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     ********************************** 处理service END ********************************
     */
}
