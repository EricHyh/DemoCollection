package com.hyh.plg.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * @author Administrator
 * @description
 * @data 2019/2/27
 */

public class BaseActivityBridge implements IActivityBridge {

    @Override
    public void onBeforeCreate(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onAfterCreate(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public boolean onRestoreInstanceState(Bundle savedInstanceState) {
        return false;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void onStateNotSaved() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPostResume() {

    }

    @Override
    public void onLocalVoiceInteractionStarted() {

    }

    @Override
    public void onLocalVoiceInteractionStopped() {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public boolean onSaveInstanceState(Bundle outState) {
        return false;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onUserLeaveHint() {

    }

    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        return false;
    }

    @Override
    public CharSequence onCreateDescription() {
        return null;
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {

    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {

    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {

    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return null;
    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onAttachFragment(Fragment fragment) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void onUserInteraction() {

    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {

    }

    @Override
    public void onContentChanged() {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    @Override
    public void onAttachedToWindow() {

    }

    @Override
    public void onDetachedFromWindow() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    @Override
    public View onCreatePanelView(int featureId) {
        return null;
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return false;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onNavigateUp() {
        return false;
    }

    @Override
    public boolean onNavigateUpFromChild(Activity child) {
        return false;
    }

    @Override
    public void onContextMenuClosed(Menu menu) {

    }

    @Override
    public Dialog onCreateDialog(int id) {
        return null;
    }

    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {

    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog, Bundle args) {

    }

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    @Override
    public boolean onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {

    }

    @Override
    public void onTitleChanged(CharSequence title, int color) {

    }

    @Override
    public void onChildTitleChanged(Activity childActivity, CharSequence title) {

    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public void onVisibleBehindCanceled() {

    }

    @Override
    public void onEnterAnimationComplete() {

    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onActionModeFinished(ActionMode mode) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean startActivityForResult(Intent intent, int requestCode, Bundle options) {
        return false;
    }

    @Override
    public boolean setTheme(int resid) {
        return false;
    }

    @Override
    public boolean setRequestedOrientation(int requestedOrientation) {
        return false;
    }

    @Override
    public AssetManager getAssets(AssetManager superAssets) {
        return null;
    }

    @Override
    public LayoutInflater getLayoutInflater(LayoutInflater superInflater) {
        return null;
    }

    @Override
    public Resources getResources(Resources superResources) {
        return null;
    }

    @Override
    public String getPackageName(String superPackageName) {
        return null;
    }

    @Override
    public PackageManager getPackageManager(PackageManager superPackageManager) {
        return null;
    }

    @Override
    public Context getBaseContext(Context superContext) {
        return null;
    }

    @Override
    public Context getApplicationContext(Context superContext) {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo(ApplicationInfo superApplicationInfo) {
        return null;
    }

    @Override
    public Object getSystemService(String name, Object superService) {
        return null;
    }
}
