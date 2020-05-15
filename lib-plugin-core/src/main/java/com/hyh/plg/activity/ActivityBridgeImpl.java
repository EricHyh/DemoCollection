package com.hyh.plg.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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

/**
 * @author Administrator
 * @description
 * @data 2019/3/11
 */

public class ActivityBridgeImpl implements IActivity {

    private Activity mActivity;
    private IActivityBridge mActivityBridge;

    @Override
    public void attach(Activity activity) {
        this.mActivity = activity;
        Intent intent = activity.getIntent();
        if (intent == null) return;
        Bundle bundle = intent.getBundleExtra(ActivityBridgeManager.ACTIVITY_BRIDGE_EXTRA_KEY);
        if (bundle == null) return;
        String hashStr = bundle.getString(ActivityBridgeManager.ACTIVITY_BRIDGE_HASH_KEY);
        if (TextUtils.isEmpty(hashStr)) return;
        mActivityBridge = ActivityBridgeManager.getInstance().removeActivityBridge(hashStr);
    }

    @Override
    public void onBeforeCreate(Bundle savedInstanceState) {
        if (mActivityBridge == null) return;
        mActivityBridge.onBeforeCreate(mActivity, savedInstanceState);
    }

    @Override
    public void onAfterCreate(Bundle savedInstanceState) {
        if (mActivityBridge == null) return;
        mActivityBridge.onAfterCreate(mActivity, savedInstanceState);
    }

    @Override
    public void onStart() {
        if (mActivityBridge == null) return;
        mActivityBridge.onStart();
    }

    @Override
    public void onRestart() {
        if (mActivityBridge == null) return;
        mActivityBridge.onRestart();
    }

    @Override
    public void onResume() {
        if (mActivityBridge == null) return;
        mActivityBridge.onResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mActivityBridge == null) return;
        mActivityBridge.onNewIntent(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mActivityBridge == null) return;
        mActivityBridge.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mActivityBridge == null) return;
        mActivityBridge.onPause();
    }

    @Override
    public void onStop() {
        if (mActivityBridge == null) return;
        mActivityBridge.onStop();
    }

    @Override
    public void onDestroy() {
        if (mActivityBridge == null) return;
        mActivityBridge.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mActivityBridge == null) return;
        mActivityBridge.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        if (mActivityBridge == null) return;
        mActivityBridge.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if (mActivityBridge == null) return;
        mActivityBridge.onTrimMemory(level);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (mActivityBridge == null) return;
        mActivityBridge.onAttachFragment(fragment);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mActivityBridge != null && mActivityBridge.dispatchKeyEvent(event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityBridge == null) return;
        mActivityBridge.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mActivityBridge == null) return;
        mActivityBridge.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        if (mActivityBridge == null) return false;
        return mActivityBridge.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mActivityBridge != null && mActivityBridge.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mActivityBridge != null && mActivityBridge.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (mActivityBridge == null) return;
        mActivityBridge.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mActivityBridge == null) return;
        mActivityBridge.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mActivityBridge != null && mActivityBridge.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActivityBridge != null && mActivityBridge.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mActivityBridge != null && mActivityBridge.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return mActivityBridge != null && mActivityBridge.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onUserLeaveHint() {
        if (mActivityBridge == null) return;
        mActivityBridge.onUserLeaveHint();
    }

    @Override
    public void onUserInteraction() {
        if (mActivityBridge == null) return;
        mActivityBridge.onUserInteraction();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return mActivityBridge != null && mActivityBridge.dispatchTouchEvent(motionEvent);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
        return mActivityBridge != null && mActivityBridge.dispatchTrackballEvent(motionEvent);
    }

    @Override
    public void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        if (mActivityBridge == null) return;
        mActivityBridge.onApplyThemeResource(theme, resid, first);
    }

    @Override
    public void onChildTitleChanged(Activity childActivity, CharSequence title) {
        if (mActivityBridge == null) return;
        mActivityBridge.onChildTitleChanged(childActivity, title);
    }

    @Override
    public void onContentChanged() {
        if (mActivityBridge == null) return;
        mActivityBridge.onContentChanged();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return mActivityBridge != null && mActivityBridge.onContextItemSelected(item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        if (mActivityBridge == null) return;
        mActivityBridge.onContextMenuClosed(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (mActivityBridge == null) return;
        mActivityBridge.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public CharSequence onCreateDescription() {
        if (mActivityBridge == null) return null;
        return mActivityBridge.onCreateDescription();
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (mActivityBridge == null) return null;
        return mActivityBridge.onCreateDialog(id);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return mActivityBridge != null && mActivityBridge.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        if (mActivityBridge == null) return null;
        return mActivityBridge.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        return mActivityBridge != null && mActivityBridge.onCreateThumbnail(outBitmap, canvas);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        if (mActivityBridge == null) return null;
        return mActivityBridge.onCreateView(name, context, attrs);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return mActivityBridge != null && mActivityBridge.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return mActivityBridge != null && mActivityBridge.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return mActivityBridge != null && mActivityBridge.onMenuOpened(featureId, menu);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        if (mActivityBridge == null) return;
        mActivityBridge.onOptionsMenuClosed(menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        if (mActivityBridge == null) return;
        mActivityBridge.onPanelClosed(featureId, menu);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        if (mActivityBridge == null) return;
        mActivityBridge.onPostCreate(savedInstanceState);
    }

    @Override
    public void onPostResume() {
        if (mActivityBridge == null) return;
        mActivityBridge.onPostResume();
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        if (mActivityBridge == null) return;
        mActivityBridge.onPrepareDialog(id, dialog);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return mActivityBridge != null && mActivityBridge.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return mActivityBridge != null && mActivityBridge.onPreparePanel(featureId, view, menu);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mActivityBridge == null) return null;
        return mActivityBridge.onRetainNonConfigurationInstance();
    }

    @Override
    public boolean onSearchRequested() {
        return mActivityBridge != null && mActivityBridge.onSearchRequested();
    }

    @Override
    public void onTitleChanged(CharSequence title, int color) {
        if (mActivityBridge == null) return;
        mActivityBridge.onTitleChanged(title, color);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        return mActivityBridge != null && mActivityBridge.onTrackballEvent(event);
    }

    @Override
    public void onFinish() {
        if (mActivityBridge == null) return;
        mActivityBridge.onFinish();
    }

    @Override
    public boolean onStartActivityForResult(Intent intent, int requestCode, Bundle options) {
        return mActivityBridge != null && mActivityBridge.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public Context onGetApplicationContext() {
        if (mActivityBridge == null) {
            return null;
        }
        return mActivityBridge.getApplicationContext(null);
    }

    @Override
    public boolean setTheme(int resid) {
        return mActivityBridge != null && mActivityBridge.setTheme(resid);
    }

    @Override
    public boolean setRequestedOrientation(int requestedOrientation) {
        return mActivityBridge != null && mActivityBridge.setRequestedOrientation(requestedOrientation);
    }
}