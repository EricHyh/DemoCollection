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
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by tangdongwei on 2018/5/14.
 */

public interface IActivity {

    public void attach(Activity activity);

    public void onBeforeCreate(Bundle savedInstanceState);

    public void onAfterCreate(Bundle savedInstanceState);

    public void onStart();

    public void onRestart();

    public void onResume();

    public void onNewIntent(Intent intent);

    public void onSaveInstanceState(Bundle outState);

//    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState);

    public void onPause();

    public void onStop();

    public void onDestroy();

    public void onConfigurationChanged(Configuration newConfig);

    public void onLowMemory();

    public void onTrimMemory(int level);

    public void onAttachFragment(Fragment fragment);

    public boolean dispatchKeyEvent(KeyEvent event);

    public void onActivityResult(int requestCode, int resultCode, Intent data);

    public void onRestoreInstanceState(Bundle savedInstanceState);

    public boolean onBackPressed();

    public boolean onTouchEvent(MotionEvent event);

    public boolean onKeyUp(int keyCode, KeyEvent event);

    public void onWindowAttributesChanged(WindowManager.LayoutParams params);

    public void onWindowFocusChanged(boolean hasFocus);

    public boolean onCreateOptionsMenu(Menu menu);

    public boolean onOptionsItemSelected(MenuItem item);

    public boolean onKeyDown(int keyCode, KeyEvent event);

    public boolean onKeyLongPress(int keyCode, KeyEvent event);

    public void onUserLeaveHint();

    public void onUserInteraction();

    /*

     */
    public boolean dispatchTouchEvent(MotionEvent motionEvent);

    public boolean dispatchTrackballEvent(MotionEvent motionEvent);

    public void onApplyThemeResource(Resources.Theme theme, int resid, boolean first);

    public void onChildTitleChanged(Activity childActivity, CharSequence title);

    public void onContentChanged();

    public boolean onContextItemSelected(MenuItem item);

    public void onContextMenuClosed(Menu menu);

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    public CharSequence onCreateDescription();

    public Dialog onCreateDialog(int id);

    public boolean onCreatePanelMenu(int featureId, Menu menu);

    public View onCreatePanelView(int featureId);

    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas);

    public View onCreateView(String name, Context context, AttributeSet attrs);

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

    public boolean onMenuItemSelected(int featureId, MenuItem item);

    public boolean onMenuOpened(int featureId, Menu menu);

    public void onOptionsMenuClosed(Menu menu);

    public void onPanelClosed(int featureId, Menu menu);

    public void onPostCreate(Bundle savedInstanceState);

    public void onPostResume();

    public void onPrepareDialog(int id, Dialog dialog);

    public boolean onPrepareOptionsMenu(Menu menu);

    public boolean onPreparePanel(int featureId, View view, Menu menu);

    public Object onRetainNonConfigurationInstance();

    public boolean onSearchRequested();

    public void onTitleChanged(CharSequence title, int color);

    public boolean onTrackballEvent(MotionEvent event);

    public void onFinish();

    public boolean onStartActivityForResult(Intent intent, int requestCode, Bundle options);

    public Context onGetApplicationContext();

    boolean setTheme(int resid);

    boolean setRequestedOrientation(int requestedOrientation);
}
