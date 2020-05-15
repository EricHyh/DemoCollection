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
 * @data 2019/2/26
 */

public interface IActivityBridge {

    void onBeforeCreate(Activity activity, Bundle savedInstanceState);

    void onAfterCreate(Activity activity, Bundle savedInstanceState);

    boolean onRestoreInstanceState(Bundle savedInstanceState);

    void onPostCreate(Bundle savedInstanceState);

    void onStart();

    void onRestart();

    void onStateNotSaved();

    void onResume();

    void onPostResume();

    void onLocalVoiceInteractionStarted();

    void onLocalVoiceInteractionStopped();

    void onNewIntent(Intent intent);

    boolean onSaveInstanceState(Bundle outState);

    void onPause();

    void onUserLeaveHint();

    boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas);

    CharSequence onCreateDescription();

    void onStop();

    void onFinish();

    void onDestroy();

    void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig);

    void onMultiWindowModeChanged(boolean isInMultiWindowMode);

    void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig);

    void onPictureInPictureModeChanged(boolean isInPictureInPictureMode);

    void onConfigurationChanged(Configuration newConfig);

    Object onRetainNonConfigurationInstance();

    void onLowMemory();

    void onTrimMemory(int level);

    void onAttachFragment(Fragment fragment);

    boolean onKeyDown(int keyCode, KeyEvent event);

    boolean onKeyLongPress(int keyCode, KeyEvent event);

    boolean onKeyUp(int keyCode, KeyEvent event);

    boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

    boolean onBackPressed();

    boolean onKeyShortcut(int keyCode, KeyEvent event);

    boolean onTouchEvent(MotionEvent event);

    boolean onTrackballEvent(MotionEvent event);

    boolean onGenericMotionEvent(MotionEvent event);

    void onUserInteraction();

    void onWindowAttributesChanged(WindowManager.LayoutParams params);

    void onContentChanged();

    void onWindowFocusChanged(boolean hasFocus);

    void onAttachedToWindow();

    void onDetachedFromWindow();

    boolean dispatchKeyEvent(KeyEvent event);

    boolean dispatchKeyShortcutEvent(KeyEvent event);

    boolean dispatchTouchEvent(MotionEvent ev);

    boolean dispatchTrackballEvent(MotionEvent ev);

    boolean dispatchGenericMotionEvent(MotionEvent ev);

    boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event);

    View onCreatePanelView(int featureId);

    boolean onCreatePanelMenu(int featureId, Menu menu);

    boolean onPreparePanel(int featureId, View view, Menu menu);

    boolean onMenuOpened(int featureId, Menu menu);

    boolean onMenuItemSelected(int featureId, MenuItem item);

    void onPanelClosed(int featureId, Menu menu);

    boolean onCreateOptionsMenu(Menu menu);

    boolean onPrepareOptionsMenu(Menu menu);

    boolean onOptionsItemSelected(MenuItem item);

    void onOptionsMenuClosed(Menu menu);

    void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    boolean onContextItemSelected(MenuItem item);

    boolean onNavigateUp();

    boolean onNavigateUpFromChild(Activity child);

    void onContextMenuClosed(Menu menu);

    Dialog onCreateDialog(int id);

    Dialog onCreateDialog(int id, Bundle args);

    void onPrepareDialog(int id, Dialog dialog);

    void onPrepareDialog(int id, Dialog dialog, Bundle args);

    boolean onSearchRequested();

    boolean onApplyThemeResource(Resources.Theme theme, int resid, boolean first);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onActivityReenter(int resultCode, Intent data);

    void onTitleChanged(CharSequence title, int color);

    void onChildTitleChanged(Activity childActivity, CharSequence title);

    View onCreateView(String name, Context context, AttributeSet attrs);

    View onCreateView(View parent, String name, Context context, AttributeSet attrs);

    void onVisibleBehindCanceled();

    void onEnterAnimationComplete();

    ActionMode onWindowStartingActionMode(ActionMode.Callback callback);

    ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type);

    void onActionModeStarted(ActionMode mode);

    void onActionModeFinished(ActionMode mode);

    void onPointerCaptureChanged(boolean hasCapture);

    boolean startActivityForResult(Intent intent, int requestCode, Bundle options);

    boolean setTheme(int resid);

    boolean setRequestedOrientation(int requestedOrientation);

    AssetManager getAssets(AssetManager superAssets);

    LayoutInflater getLayoutInflater(LayoutInflater superInflater);

    Resources getResources(Resources superResources);

    String getPackageName(String superPackageName);

    PackageManager getPackageManager(PackageManager superPackageManager);

    Context getBaseContext(Context superContext);

    Context getApplicationContext(Context superContext);

    ApplicationInfo getApplicationInfo(ApplicationInfo superApplicationInfo);

    Object getSystemService(String name, Object superService);
}