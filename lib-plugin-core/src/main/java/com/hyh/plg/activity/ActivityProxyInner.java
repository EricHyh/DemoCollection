package com.hyh.plg.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.yly.mob.android.BlockApplication;
import com.yly.mob.utils.Logger;

import java.util.Map;

/**
 * Created by tangdongwei on 2018/5/14.
 */

public class ActivityProxyInner extends Activity {

    private static final String EXTRA_KEY_OF_INTENT = "extra_key_of_intent";

    private Object noGc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i("--------------ActivityProxyInner running-----------------");
        noGc = callBlock(ActivityProxyKey.ATTACH, null, new Object[]{this});
        callBlock(ActivityProxyKey.ON_BEFORE_CREATE, null, new Object[]{this, savedInstanceState});
        super.onCreate(savedInstanceState);
        callBlock(ActivityProxyKey.ON_AFTER_CREATE, null, new Object[]{this, savedInstanceState});
    }

    @Override
    public void onStart() {
        super.onStart();
        callBlock(ActivityProxyKey.ON_START, null, new Object[]{this});
    }

    @Override
    public void onRestart() {
        super.onRestart();
        callBlock(ActivityProxyKey.ON_RESTART, null, new Object[]{this});
    }

    @Override
    public void onResume() {
        super.onResume();
        callBlock(ActivityProxyKey.ON_RESUME, null, new Object[]{this});
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        callBlock(ActivityProxyKey.ON_NEW_INTENT, null, new Object[]{this, intent});
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
        callBlock(ActivityProxyKey.ON_SAVE_INSTANCE_STATE_1, null, new Object[]{this, outState});
    }

    @Override
    public void onPause() {
        super.onPause();
        callBlock(ActivityProxyKey.ON_PAUSE, null, new Object[]{this});
    }

    @Override
    public void onStop() {
        super.onStop();
        callBlock(ActivityProxyKey.ON_STOP, null, new Object[]{this});
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callBlock(ActivityProxyKey.ON_DESTROY, null, new Object[]{this});
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        callBlock(ActivityProxyKey.ON_CONFIGURATION_CHANGED, null, new Object[]{this, newConfig});
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        callBlock(ActivityProxyKey.ON_LOW_MEMORY, null, new Object[]{this});
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        callBlock(ActivityProxyKey.ON_TRIM_MEMORY, null, new Object[]{this, level});
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        callBlock(ActivityProxyKey.ON_ATTACH_FRAGMENT, null, new Object[]{this, fragment});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callBlock(ActivityProxyKey.ON_ACTIVITY_RESULT, null, new Object[]{this, requestCode, resultCode, data});
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
        callBlock(ActivityProxyKey.ON_RESTORE_INSTANCE_STATE, null, new Object[]{this, savedInstanceState});
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        callBlock(ActivityProxyKey.ON_USER_LEAVE_HINT, null, new Object[]{this});
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        callBlock(ActivityProxyKey.ON_USER_INTERACTION, null, new Object[]{this});
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        callBlock(ActivityProxyKey.ON_BACK_PRESSED, null, new Object[]{this});
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Object result = callBlock(ActivityProxyKey.ON_TOUCH_EVENT, null, new Object[]{this, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Object result = callBlock(ActivityProxyKey.ON_KEY_UP, null, new Object[]{this, keyCode, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        super.onWindowAttributesChanged(params);
        callBlock(ActivityProxyKey.ON_WINDOW_ATTRIBUTES_CHANGED, null, new Object[]{this, params});
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        callBlock(ActivityProxyKey.ON_WINDOW_FOCUS_CHANGED, null, new Object[]{this, hasFocus});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Object result = callBlock(ActivityProxyKey.ON_CREATE_OPTIONS_MENU, null, new Object[]{this, menu});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Object result = callBlock(ActivityProxyKey.ON_OPTIONS_ITEM_SELECTED, null, new Object[]{this, item});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Object result = callBlock(ActivityProxyKey.ON_KEY_DOWN, null, new Object[]{this, keyCode, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Object result = callBlock(ActivityProxyKey.ON_KEY_LONG_PRESS, null, new Object[]{this, keyCode, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onKeyLongPress(keyCode, event);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Object result = callBlock(ActivityProxyKey.DISPATCH_KEY_EVENT, null, new Object[]{this, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    /*
     **********
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Object result = callBlock(ActivityProxyKey.dispatchTouchEvent, null, new Object[]{this, ev});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        Object result = callBlock(ActivityProxyKey.dispatchTrackballEvent, null, new Object[]{this, motionEvent});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.dispatchTrackballEvent(motionEvent);
        }
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
        callBlock(ActivityProxyKey.onApplyThemeResource, null, new Object[]{this, theme, resid, first});
    }

    @Override
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
        super.onChildTitleChanged(childActivity, title);
        callBlock(ActivityProxyKey.onChildTitleChanged, null, new Object[]{this, childActivity, title});
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        callBlock(ActivityProxyKey.onContentChanged, null, new Object[]{this});
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Object result = callBlock(ActivityProxyKey.onContextItemSelected, null, new Object[]{this, item});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        callBlock(ActivityProxyKey.onContextMenuClosed, null, new Object[]{this, menu});
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        callBlock(ActivityProxyKey.onCreateContextMenu, null, new Object[]{this, menu, v, menuInfo});

    }

    @Override
    public CharSequence onCreateDescription() {
        Object result = callBlock(ActivityProxyKey.onCreateDescription, null, new Object[]{this});
        if (result != null) {
            return (CharSequence) result;
        } else {
            return super.onCreateDescription();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Object result = callBlock(ActivityProxyKey.onCreateDialog, null, new Object[]{this, id});
        if (result != null) {
            return (Dialog) result;
        } else {
            return super.onCreateDialog(id);
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        Object result = callBlock(ActivityProxyKey.onCreatePanelMenu, null, new Object[]{this, featureId, menu});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onCreatePanelMenu(featureId, menu);
        }
    }

    @Override
    public View onCreatePanelView(int featureId) {
        Object result = callBlock(ActivityProxyKey.onCreatePanelView, null, new Object[]{this, featureId});
        if (result != null) {
            return (View) result;
        } else {
            return super.onCreatePanelView(featureId);
        }
    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        Object result = callBlock(ActivityProxyKey.onCreateThumbnail, null, new Object[]{this, outBitmap, canvas});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onCreateThumbnail(outBitmap, canvas);
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        Object result = callBlock(ActivityProxyKey.onCreateView, null, new Object[]{this, name, context, attrs});
        if (result != null) {
            return (View) result;
        } else {
            return super.onCreateView(name, context, attrs);
        }
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        Object result = callBlock(ActivityProxyKey.onKeyMultiple, null, new Object[]{this, keyCode, repeatCount, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Object result = callBlock(ActivityProxyKey.onMenuItemSelected, null, new Object[]{this, featureId, item});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        Object result = callBlock(ActivityProxyKey.onMenuOpened, null, new Object[]{this, featureId, menu});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onMenuOpened(featureId, menu);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        callBlock(ActivityProxyKey.onOptionsMenuClosed, null, new Object[]{this, menu});
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        callBlock(ActivityProxyKey.onPanelClosed, null, new Object[]{this, featureId, menu});
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        callBlock(ActivityProxyKey.onPostCreate, null, new Object[]{this, savedInstanceState});
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        callBlock(ActivityProxyKey.onPostResume, null, new Object[]{this});
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        callBlock(ActivityProxyKey.onPrepareDialog, null, new Object[]{this, id, dialog});
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Object result = callBlock(ActivityProxyKey.onPrepareOptionsMenu, null, new Object[]{this, menu});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        Object result = callBlock(ActivityProxyKey.onPreparePanel, null, new Object[]{this, featureId, view, menu});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onPreparePanel(featureId, view, menu);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        Object result = callBlock(ActivityProxyKey.onRetainNonConfigurationInstance, null, new Object[]{this});
        if (result != null) {
            return result;
        } else {
            return super.onRetainNonConfigurationInstance();
        }
    }

    @Override
    public boolean onSearchRequested() {
        Object result = callBlock(ActivityProxyKey.onSearchRequested, null, new Object[]{this});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onSearchRequested();
        }
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        callBlock(ActivityProxyKey.onTitleChanged, null, new Object[]{this, title, color});
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        Object result = callBlock(ActivityProxyKey.onTrackballEvent, null, new Object[]{this, event});
        if (result != null && (boolean) result) {
            return true;
        } else {
            return super.onTrackballEvent(event);
        }
    }

    /*
     *************
     */

    @Override
    public Context getApplicationContext() {
        Object result = callBlock(ActivityProxyKey.GET_APPLICATION_CONTEXT, null, new Object[]{this});
        return result == null ? super.getApplicationContext() : (Context) result;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (intent != null && intent.getStringExtra(EXTRA_KEY_OF_INTENT) != null) {
            super.startActivityForResult(intent, requestCode, options);
        } else {
            callBlock(ActivityProxyKey.START_ACTIVITY_FOR_RESULT, null, new Object[]{this, intent, requestCode, options});
        }
    }

    @Override
    public FragmentManager getFragmentManager() {
        Object result = callBlock(ActivityProxyKey.GET_FRAGMENT_MANAGER, null, new Object[]{this});
        if (result == null || (result instanceof String && TextUtils.equals("super", (String) result))) {
            return super.getFragmentManager();
        } else {
            return (FragmentManager) result;
        }
    }

    @Override
    public PackageManager getPackageManager() {
        Object result = callBlock(ActivityProxyKey.getPackageManager, null, new Object[]{this});
        if (result == null || (result instanceof String && TextUtils.equals("super", (String) result))) {
            return super.getPackageManager();
        } else {
            return (PackageManager) result;
        }
    }

    private Object callBlock(String command, Map params, Object... masterClients) {
        if (BlockApplication.blockApplicationContext != null && BlockApplication.hostClassLoader != null) {
            return ActivityProxyImpl.scheduleHandleActivityProxy(BlockApplication.blockApplicationContext,
                      BlockApplication.hostClassLoader, command, params, masterClients);
        } else {
            return null;
        }
    }
}
