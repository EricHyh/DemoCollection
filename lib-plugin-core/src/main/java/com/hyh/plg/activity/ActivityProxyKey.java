package com.hyh.plg.activity;

/**
 * Created by tangdongwei on 2018/5/21.
 */

public class ActivityProxyKey {

    public static final String ATTACH = "ActivityProxy_attach()";

    public static final String ON_BEFORE_CREATE = "ActivityProxy_onCreate_onBeforeCreate(Bundle savedInstanceState)";

    public static final String ON_AFTER_CREATE = "ActivityProxy_onCreate_onAfterCreate(Bundle savedInstanceState)";

    public static final String ON_START = "ActivityProxy_onStart()";

    public static final String ON_RESTART = "ActivityProxy_onRestart()";

    public static final String ON_RESUME = "ActivityProxy_onResume()";

    public static final String ON_NEW_INTENT = "ActivityProxy_onNewIntent(Intent intent)";

    public static final String ON_SAVE_INSTANCE_STATE_1 = "ActivityProxy_onSaveInstanceState(Bundle outState)";

    public static final String ON_PAUSE = "ActivityProxy_onPause()";

    public static final String ON_STOP = "ActivityProxy_onStop()";

    public static final String ON_DESTROY = "ActivityProxy_onDestroy()";

    public static final String ON_CONFIGURATION_CHANGED = "ActivityProxy_onConfigurationChanged(Configuration newConfig)";

    public static final String ON_LOW_MEMORY = "ActivityProxy_onLowMemory()";

    public static final String ON_TRIM_MEMORY = "ActivityProxy_onTrimMemory(int level)";

    public static final String ON_ATTACH_FRAGMENT = "ActivityProxy_onAttachFragment(Fragment fragment)";

    public static final String ON_ACTIVITY_RESULT = "ActivityProxy_onActivityResult(int requestCode, int resultCode, Intent data)";

    public static final String ON_RESTORE_INSTANCE_STATE = "ActivityProxy_onRestoreInstanceState(Bundle savedInstanceState)";

    public static final String ON_BACK_PRESSED = "ActivityProxy_onBackPressed()";

    public static final String ON_TOUCH_EVENT = "ActivityProxy_onTouchEvent(MotionEvent event)";

    public static final String ON_KEY_UP = "ActivityProxy_onKeyUp(int keyCode, KeyEvent event)";

    public static final String ON_WINDOW_ATTRIBUTES_CHANGED = "ActivityProxy_onWindowAttributesChanged(WindowManager.LayoutParams params)";

    public static final String ON_WINDOW_FOCUS_CHANGED = "ActivityProxy_onWindowFocusChanged(boolean hasFocus)";

    public static final String ON_CREATE_OPTIONS_MENU = "ActivityProxy_onCreateOptionsMenu(Menu menu)";

    public static final String ON_OPTIONS_ITEM_SELECTED = "ActivityProxy_onOptionsItemSelected(MenuItem item)";

    public static final String ON_KEY_DOWN = "ActivityProxy_onKeyDown(int keyCode, KeyEvent event)";

    public static final String ON_KEY_LONG_PRESS = "ActivityProxy_onKeyLongPress(int keyCode, KeyEvent event)";

    public static final String DISPATCH_KEY_EVENT = "ActivityProxy_dispatchKeyEvent(KeyEvent event)";

    public static final String GET_APPLICATION_CONTEXT = "ActivityProxy_getApplicationContext()";

    public static final String START_ACTIVITY_FOR_RESULT = "ActivityProxy_startActivityForResult(Intent intent, int requestCode, Bundle options)";

    public static final String GET_FRAGMENT_MANAGER = "ActivityProxy_getFragmentManager()";

    public static final String ON_USER_LEAVE_HINT = "ActivityProxy_onUserLeaveHint()";

    public static final String ON_USER_INTERACTION = "ActivityProxy_onUserInteraction()";

    /*

     */
    public static final String dispatchTouchEvent = "ActivityProxy_dispatchTouchEvent(MotionEvent motionEvent)";

    public static final String dispatchTrackballEvent = "ActivityProxy_dispatchTrackballEvent(MotionEvent motionEvent)";

    public static final String onApplyThemeResource = "ActivityProxy_onApplyThemeResource(Resources.Theme theme, int var2, boolean var3)";

    public static final String onChildTitleChanged = "ActivityProxy_onChildTitleChanged(Activity var1, CharSequence var2)";

    public static final String onContentChanged = "ActivityProxy_onContentChanged()";

    public static final String onContextItemSelected = "ActivityProxy_onContextItemSelected(MenuItem var1)";

    public static final String onContextMenuClosed = "ActivityProxy_onContextMenuClosed(Menu var1)";

    public static final String onCreateContextMenu = "ActivityProxy_onCreateContextMenu(ContextMenu var1, View var2, ContextMenu.ContextMenuInfo var3)";

    public static final String onCreateDescription = "ActivityProxy_onCreateDescription()";

    public static final String onCreateDialog = "ActivityProxy_onCreateDialog(int var1)";

    public static final String onCreatePanelMenu = "ActivityProxy_onCreatePanelMenu(int var1, Menu var2)";

    public static final String onCreatePanelView = "ActivityProxy_onCreatePanelView(int var1)";

    public static final String onCreateThumbnail = "ActivityProxy_onCreateThumbnail(Bitmap var1, Canvas var2)";

    public static final String onCreateView = "ActivityProxy_onCreateView(String var1, Context var2, AttributeSet var3)";

    public static final String onKeyMultiple = "ActivityProxy_onKeyMultiple(int var1, int var2, KeyEvent var3)";

    public static final String onMenuItemSelected = "ActivityProxy_onMenuItemSelected(int var1, MenuItem var2)";

    public static final String onMenuOpened = "ActivityProxy_onMenuOpened(int var1, Menu var2)";

    public static final String onOptionsMenuClosed = "ActivityProxy_onOptionsMenuClosed(Menu var1)";

    public static final String onPanelClosed = "ActivityProxy_onPanelClosed(int var1, Menu var2)";

    public static final String onPostCreate = "ActivityProxy_onPostCreate(Bundle var1)";

    public static final String onPostResume = "ActivityProxy_onPostResume()";

    public static final String onPrepareDialog = "ActivityProxy_onPrepareDialog(int var1, Dialog var2)";

    public static final String onPrepareOptionsMenu = "ActivityProxy_onPrepareOptionsMenu(Menu var1)";

    public static final String onPreparePanel = "ActivityProxy_onPreparePanel(int var1, View var2, Menu var3)";

    public static final String onRetainNonConfigurationInstance = "ActivityProxy_onRetainNonConfigurationInstance()";

    public static final String onSearchRequested = "ActivityProxy_onSearchRequested()";

    public static final String onTitleChanged = "ActivityProxy_onTitleChanged(CharSequence var1, int var2)";

    public static final String onTrackballEvent = "ActivityProxy_onTrackballEvent(MotionEvent var1)";

    public static final String getPackageManager = "ActivityProxy_getPackageManager()";

}
