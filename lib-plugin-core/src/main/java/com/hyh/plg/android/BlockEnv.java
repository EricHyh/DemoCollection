package com.hyh.plg.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.hyh.plg.activity.transform.ProxyActivityTransformation;
import com.hyh.plg.activity.transform.Transformation;
import com.hyh.plg.utils.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tangdongwei on 2018/11/28.
 */
public class BlockEnv {

    public static ProxyActivityInfo sProxyActivityInfo;

    public static String sProxyService;

    public static ClassLoader sHostClassLoader;
    public static Context sHostApplicationContext;
    public static String sBlockPath;
    public static Context sBlockApplicationContext;
    public static Application sBlockApplication;
    public static PackageInfo sBlockPackageInfo;

    public static List<Transformation> sBeginTransformationList = new CopyOnWriteArrayList<>();
    public static final Transformation sProxyActivityTransformation = new ProxyActivityTransformation();
    public static List<Transformation> sMidTransformationList = new CopyOnWriteArrayList<>();
    public static Transformation sEndTransformation;

    public static final Map<String, Context> sBlockActivityBaseContextMap = new ConcurrentHashMap<>();
    public static final Map<String, Context> sBlockServiceBaseContextMap = new ConcurrentHashMap<>();
    public static final Map<String, Context> sBlockBroadcastBaseContextMap = new ConcurrentHashMap<>();

    //全功能
    public static final int MODE_ALL_FUNCTION = 0xffffffff;

    //代理activity
    public static final int MODE_ACTIVITY_PROXY = 1;

    //代理service
    public static final int MODE_SERVICE_PROXY = 1 << 1;

    //代理packageManager
    public static final int MODE_PACKAGE_MANAGER_PROXY = 1 << 2;

    //默认打开代理activity的功能，因为现在线上需要使用代理activity，但是不需要使用代理service和代理packageManager
    public static int sFunctionModeFlag = MODE_ACTIVITY_PROXY;

    public static void setFunctionMode(int flags) {
        sFunctionModeFlag = flags;
    }

    public static void addFunctionModeFlags(int flags) {
        sFunctionModeFlag |= flags;
    }

    public static void removeFunctionModeFlags(int flags) {
        sFunctionModeFlag &= ~flags;
    }

    public static boolean isActivityProxyEnable() {
        boolean result = (sFunctionModeFlag & MODE_ACTIVITY_PROXY) != 0;
        Logger.v("IN BlockEnv, isActivityProxyEnable=" + result);
        return result;
    }

    public static boolean isServiceProxyEnable() {
        boolean result = (sFunctionModeFlag & MODE_SERVICE_PROXY) != 0;
        Logger.v("IN BlockEnv, isServiceProxyEnable=" + result);
        return result;
    }

    public static boolean isPackageManagerProxyEnable() {
        boolean result = (sFunctionModeFlag & MODE_PACKAGE_MANAGER_PROXY) != 0;
        Logger.v("IN BlockEnv, isPackageManagerProxyEnable=" + result);
        return result;
    }

    public static void addBeginTransformation(Transformation transformation) {
        if (sBeginTransformationList.contains(transformation)) return;
        sBeginTransformationList.add(transformation);
    }

    public static void removeBeginTransformation(Transformation transformation) {
        sBeginTransformationList.remove(transformation);
    }

    @Deprecated
    public static void addActivityTransformation(Transformation transformation) {
        if (sMidTransformationList.contains(transformation)) return;
        sMidTransformationList.add(transformation);
    }

    public static void addMidTransformation(Transformation transformation) {
        if (sMidTransformationList.contains(transformation)) return;
        sMidTransformationList.add(transformation);
    }

    public static void removeMidTransformation(Transformation transformation) {
        sMidTransformationList.remove(transformation);
    }

    public static void setEndTransformation(Transformation transformation) {
        sEndTransformation = transformation;
    }

    public static PackageInfo newBlockPackageInfo() {
        try {
            return sHostApplicationContext.getPackageManager().getPackageArchiveInfo(sBlockPath, PackageManager.GET_ACTIVITIES
                    | PackageManager.GET_SERVICES | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS | PackageManager.GET_META_DATA);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setBlockActivityBaseContext(String activityName, Application context) {
        sBlockActivityBaseContextMap.put(activityName, context);
    }

    public static void setBlockServiceBaseContext(String serviceName, Application context) {
        sBlockServiceBaseContextMap.put(serviceName, context);
    }

    public static void setBlockBroadcastBaseContext(String broadcastName, Application context) {
        sBlockBroadcastBaseContextMap.put(broadcastName, context);
    }

    public static void setProxyActivityIntentFlags(Intent intent, int flags) {
        intent.putExtra("__proxy_activity_flags__", flags);
    }

    public static int getProxyActivityIntentFlags(Intent intent, int defaultValue) {
        return intent.getIntExtra("__proxy_activity_flags__", defaultValue);
    }
}