package com.hyh.plg.android;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;

import com.hyh.plg.api.BlockEnv;
import com.yly.mob.activity.ActivityProxyImpl;
import com.yly.mob.activity.transform.TransformIntent;
import com.yly.mob.hook.instrumentation.ApplicationInstrumentation;
import com.yly.mob.reflect.Reflect;
import com.yly.mob.service.BlockServiceServer;

import java.lang.ref.WeakReference;

/**
 * Created by tangdongwei on 2018/6/25.
 */

public class BaseBlockApplication extends Application {

    private SparseArray<WeakReference<BroadcastReceiver>> mReferenceReceiverArray = new SparseArray<>();

    public BaseBlockApplication(Context context) {
        attachBaseContext(context);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (isBlockReceiver(receiver)) {
            DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
            return super.registerReceiver(dynamicReceiverProxy, filter);
        } else {
            return super.registerReceiver(receiver, filter);
        }
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
        if (isBlockReceiver(receiver)) {
            DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
            return super.registerReceiver(dynamicReceiverProxy, filter, flags);
        } else {
            return super.registerReceiver(receiver, filter, flags);
        }
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (isBlockReceiver(receiver)) {
            DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
            return super.registerReceiver(dynamicReceiverProxy, filter, broadcastPermission, scheduler);
        } else {
            return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
        }
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, int flags) {
        if (isBlockReceiver(receiver)) {
            DynamicReceiverProxy dynamicReceiverProxy = getDynamicReceiverProxy(receiver);
            return super.registerReceiver(dynamicReceiverProxy, filter, broadcastPermission, scheduler, flags);
        } else {
            return super.registerReceiver(receiver, filter, broadcastPermission, scheduler, flags);
        }
    }

    private DynamicReceiverProxy getDynamicReceiverProxy(BroadcastReceiver receiver) {
        if (receiver == null) return null;
        DynamicReceiverProxy dynamicReceiverProxy = new DynamicReceiverProxy(receiver);
        mReferenceReceiverArray.put(receiver.hashCode(), new WeakReference<BroadcastReceiver>(dynamicReceiverProxy));
        return dynamicReceiverProxy;
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (isBlockReceiver(receiver)) {
            int key = receiver.hashCode();
            WeakReference<BroadcastReceiver> receiverReference = mReferenceReceiverArray.get(key);
            if (receiverReference != null) {
                BroadcastReceiver broadcastReceiver = receiverReference.get();
                if (broadcastReceiver != null) {
                    super.unregisterReceiver(broadcastReceiver);
                }
            }
            mReferenceReceiverArray.remove(key);
        } else {
            super.unregisterReceiver(receiver);
        }
    }

    private boolean isBlockReceiver(BroadcastReceiver receiver) {
        if (receiver != null) {
            ClassLoader receiverClassLoader = receiver.getClass().getClassLoader();
            ClassLoader currentClassLoader = getClass().getClassLoader();
            if (receiverClassLoader == currentClassLoader) {
                return true;
            }
            if (Reflect.isChildClassLoader(receiverClassLoader, currentClassLoader)) {
                return true;
            }
        }
        return false;
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
}