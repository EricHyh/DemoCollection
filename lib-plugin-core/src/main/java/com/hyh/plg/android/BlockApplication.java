package com.hyh.plg.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;

import com.hyh.plg.activity.ActivityProxyImpl;
import com.hyh.plg.activity.transform.TransformIntent;
import com.hyh.plg.android.pms.PackageManagerFactory;
import com.hyh.plg.android.pms.PackageManagerHelper;
import com.hyh.plg.hook.instrumentation.ApplicationInstrumentation;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.service.BlockServiceServer;
import com.hyh.plg.utils.Logger;

import java.lang.ref.WeakReference;

/**
 * Created by tangdongwei on 2018/6/25.
 */

public class BlockApplication extends Application {


    public static final int DEFAULT_THEME_RESOURCE = android.R.style.Theme_Holo_Light_NoActionBar;

    public static ClassLoader hostClassLoader;
    public static Context hostApplicationContext;
    public static Context blockApplicationContext;
    public static BlockApplication blockApplication;

    private Application mHostApplication;
    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;
    private LayoutInflater mLayoutInflater;
    private SparseArray<WeakReference<BroadcastReceiver>> mReferenceReceiverArray = new SparseArray<>();
    private PackageManager mPackageManager;
    private PackageManagerHelper mPackageManagerHelper;


    public BlockApplication(Context context, String blockPath) {
        attachBaseContext(context);
        this.mAssetManager = ResourceLoader.createAssetManager(blockPath);
        if (mAssetManager == null) {
            Logger.d("BlockApplication: create AssetManager failed");
            return;
        }
        mHostApplication = getHostApplication(context);
        if (mHostApplication != null) {
            copyFieldFromRealApplication(mHostApplication);
        }

        mResources = ResourceLoader.getBundleResource(context, mAssetManager);

        mLayoutInflater = LayoutInflater.from(context).cloneInContext(this);
        mLayoutInflater.setFactory2(new LayoutFactory());
        blockApplication = this;
        BlockEnv.sBlockApplication = this;
        mPackageManager = PackageManagerFactory.create(this, mHostApplication.getPackageManager());

        mPackageManagerHelper = new PackageManagerHelper();
    }

    private Application getHostApplication(Context context) {
        Application realApplication = null;
        if (context instanceof Application) {
            realApplication = (Application) context;
        } else {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext instanceof Application) {
                realApplication = (Application) applicationContext;
            }
        }
        return realApplication;
    }

    private void copyFieldFromRealApplication(Application application) {
        /**
         *
         * private ArrayList<ComponentCallbacks> mComponentCallbacks = new ArrayList<ComponentCallbacks>();
         * private ArrayList<ActivityLifecycleCallbacks> mActivityLifecycleCallbacks = new ArrayList<ActivityLifecycleCallbacks>();
         * private ArrayList<OnProvideAssistDataListener> mAssistCallbacks = null;
         * public LoadedApk mLoadedApk;
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            OnProvideAssistDataListener provideAssistDataListener = new OnProvideAssistDataListener() {
                @Override
                public void onProvideAssistData(Activity activity, Bundle data) {
                }
            };
            application.registerOnProvideAssistDataListener(provideAssistDataListener);
            application.unregisterOnProvideAssistDataListener(provideAssistDataListener);
        }
        String fieldNameArray[] = {"mComponentCallbacks", "mActivityLifecycleCallbacks", "mAssistCallbacks", "mLoadedApk"};
        for (String fieldName : fieldNameArray) {
            boolean copy = Reflect.copyField(application, this, fieldName);
            Logger.d("IN copyFieldFromRealApplication: " + fieldName + ", copy result = " + copy);
        }
    }

    @Override
    public AssetManager getAssets() {
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    public void setDefaultTheme(int resid) {
        mTheme = mResources.newTheme();
        mTheme.applyStyle(resid, true);
    }

    @Override
    public void setTheme(int resid) {
        mTheme.applyStyle(resid, true);
    }

    @Override
    public Resources.Theme getTheme() {
        return mTheme;
    }

    @Override
    public Object getSystemService(String name) {
        if (TextUtils.equals(Context.LAYOUT_INFLATER_SERVICE, name)) {
            return mLayoutInflater;
        }
        return super.getSystemService(name);
    }

    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        Logger.d("BlockApplication createConfigurationContext");
        Context configurationContext = super.createConfigurationContext(overrideConfiguration);
        return new BlockConfigurationContext(configurationContext);
    }

    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        super.getApplicationContext().registerComponentCallbacks(callback);
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        super.getApplicationContext().unregisterComponentCallbacks(callback);
    }


    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if (mHostApplication != null) {
            mHostApplication.registerActivityLifecycleCallbacks(callback);
        } else {
            super.registerActivityLifecycleCallbacks(callback);
        }
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if (mHostApplication != null) {
            mHostApplication.unregisterActivityLifecycleCallbacks(callback);
        } else {
            super.unregisterActivityLifecycleCallbacks(callback);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        if (mHostApplication != null) {
            mHostApplication.registerOnProvideAssistDataListener(callback);
        } else {
            super.registerOnProvideAssistDataListener(callback);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        if (mHostApplication != null) {
            mHostApplication.unregisterOnProvideAssistDataListener(callback);
        } else {
            super.unregisterOnProvideAssistDataListener(callback);
        }
    }


    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);

        return super.registerReceiver(dynamicReceiverProxy, filter);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
        return super.registerReceiver(dynamicReceiverProxy, filter, flags);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
        return super.registerReceiver(dynamicReceiverProxy, filter, broadcastPermission, scheduler);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
        return super.registerReceiver(dynamicReceiverProxy, filter, broadcastPermission, scheduler, flags);
    }

    private DynamicReceiverProxy getDynamicReceiverProxy(BroadcastReceiver receiver) {
        if (receiver == null) return null;
        DynamicReceiverProxy dynamicReceiverProxy = new DynamicReceiverProxy(receiver);
        mReferenceReceiverArray.put(receiver.hashCode(), new WeakReference<BroadcastReceiver>(dynamicReceiverProxy));
        return dynamicReceiverProxy;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        int key = receiver.hashCode();
        WeakReference<BroadcastReceiver> receiverReference = mReferenceReceiverArray.get(key);
        if (receiverReference != null) {
            BroadcastReceiver broadcastReceiver = receiverReference.get();
            if (broadcastReceiver != null) {
                super.unregisterReceiver(broadcastReceiver);
            }
        }
        mReferenceReceiverArray.remove(key);
    }

    //ontextImpl获取LoadApk中的ApplicationInfo只有最基本的信息，比如apk的路径，版本信息，本来就没有metadata的数据
    //跟安卓保持一致，没必要修改metaData
    @Override
    public ApplicationInfo getApplicationInfo() {
        ApplicationInfo applicationInfo = mHostApplication.getApplicationInfo();
        return mPackageManagerHelper.makeBlockApplicationInfoBaseOnHost(applicationInfo, false);
    }

    /*
     ********************************** 处理activity BEGIN ********************************
     */

    @Override
    public void startActivity(Intent intent) {
        if (!BlockEnv.isActivityProxyEnable()) {
            super.startActivity(intent);
            return;
        }
        TransformIntent transformIntent = ActivityProxyImpl.transformIntentForActivityProxy(intent);
        if (transformIntent == null) {
            super.startActivity(intent);
        } else {
            if (transformIntent.transformIntent == null) {
                return;
            }
            /*HookInstaller hookInstaller = new HookInstaller();
            hookInstaller.install();*/
            ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
            super.startActivity(transformIntent.transformIntent);
        }
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
        if (!BlockEnv.isActivityProxyEnable()) {
            super.startActivity(intent, options);
            return;
        }
        TransformIntent transformIntent = ActivityProxyImpl.transformIntentForActivityProxy(intent);
        if (transformIntent == null) {
            super.startActivity(intent, options);
        } else {
            if (transformIntent.transformIntent == null) {
                return;
            }
            /*HookInstaller hookInstaller = new HookInstaller();
            hookInstaller.install();*/
            ApplicationInstrumentation.getInstance().requestReplaceInstrumentation();
            super.startActivity(transformIntent.transformIntent, options);
        }
    }

    /*
     ********************************** 处理activity BEGIN ********************************
     */

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

    @Override
    public PackageManager getPackageManager() {
        if (!BlockEnv.isPackageManagerProxyEnable()) {
            return super.getPackageManager();
        }
        return mPackageManager;
    }
}