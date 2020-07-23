package com.hyh.plg.android.pms;


import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.hyh.plg.android.BlockApplication;
import com.hyh.plg.api.BlockEnv;
import com.hyh.plg.utils.FileUtil;
import com.hyh.plg.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangdongwei on 2018/11/23.
 */
public class PackageManagerHelper {

    //只获取一次即可，这个目录不会变
    private String mNativeLibraryDir = "";

    protected static boolean checkIntentComponent(Intent intent) {
        if (intent == null) {
            return false;
        }
        ComponentName component = intent.getComponent();
        if (component == null || TextUtils.isEmpty(component.getPackageName()) || TextUtils.isEmpty(component.getClassName())) {
            return false;
        }
        return true;
    }

    public ApplicationInfo makeBlockApplicationInfoBaseOnHost(ApplicationInfo hostApplicationInfo, boolean fixMetaData) {
        if (hostApplicationInfo == null) {
            return null;
        }
//        Logger.v("IN PackageManagerHelper, makeBlockApplicationInfoBaseOnHost for : " + hostApplicationInfo.packageName);
        //fix nativeLibraryDir
        if (TextUtils.isEmpty(mNativeLibraryDir)) {
            mNativeLibraryDir = FileUtil.getLibraryPath(BlockApplication.hostApplicationContext, BlockEnv.sBlockPackageInfo.packageName);
        }
        hostApplicationInfo.nativeLibraryDir = mNativeLibraryDir;
        //fix mataData
        if (fixMetaData) {
            if (BlockEnv.sBlockPackageInfo != null && BlockEnv.sBlockPackageInfo.applicationInfo != null
                    && BlockEnv.sBlockPackageInfo.applicationInfo.metaData != null) {
                Bundle metaData = BlockEnv.sBlockPackageInfo.applicationInfo.metaData;
                if (hostApplicationInfo.metaData != null) {
                    hostApplicationInfo.metaData.putAll(metaData);
                } else {
                    hostApplicationInfo.metaData = metaData;
                }
            }
        }

        return hostApplicationInfo;
    }

    protected ResolveInfo getBlockResolveActivityOrService(Intent intent) {
        ResolveInfo resolveInfo = getBlockResolveActivity(intent, 0);
        if (resolveInfo == null) {
            resolveInfo = getBlockResolveService(intent, 0);
        }
        return resolveInfo;
    }

    protected ResolveInfo getBlockResolveActivity(Intent intent, int flags) {
        try {
            ActivityInfo[] activities = BlockEnv.sBlockPackageInfo.activities;
            if (activities == null || activities.length < 0) {
                return null;
            }
            ComponentName component = intent.getComponent();
            if (component == null) return null;
            String queryPackageName = component.getPackageName();
            String queryClassName = component.getClassName();
            String blockPackageName = BlockEnv.sBlockPackageInfo.packageName;
            String hostPackageName = BlockApplication.hostApplicationContext.getPackageName();
            //不知道穿进来的包名是插件的还是宿主的，所以只要是其中之一就算是，如果两个都不是，才认为不是在查找宿主的组件
            if (!TextUtils.equals(queryPackageName, blockPackageName)
                    && !TextUtils.equals(queryPackageName, hostPackageName)) {
                return null;
            }
            Logger.v("IN PackageManagerHelper, getBlockResolveActivity for : " + intent);
            ActivityInfo activityInfo;
            for (int i = 0; i < activities.length; i++) {
                activityInfo = activities[i];
                if (TextUtils.equals(queryPackageName, activityInfo.packageName)) {
                    ResolveInfo resolveInfo = new ResolveInfo();
                    resolveInfo.activityInfo = activityInfo;
                    resolveInfo.resolvePackageName = queryPackageName;
                    return resolveInfo;
                }
            }
            return null;
        } catch (Exception e) {
            Logger.e("IN PackageManagerHelper, getBlockResolveActivity() ERROR : " + e.toString());
            return null;
        }
    }

    protected List<ResolveInfo> addBlockIntentActivities(List<ResolveInfo> resolveInfos, Intent intent, int flags) {
        try {
            //插件的activity
            ActivityInfo[] activities = BlockEnv.sBlockPackageInfo.activities;
            if (activities == null || activities.length < 0) {
                return resolveInfos;
            }
            ComponentName component = intent.getComponent();
            if (component == null) return resolveInfos;
            String queryPackageName = component.getPackageName();
            String queryClassName = component.getClassName();
            String blockPackageName = BlockEnv.sBlockPackageInfo.packageName;
            String hostPackageName = BlockApplication.hostApplicationContext.getPackageName();
            //不知道穿进来的包名是插件的还是宿主的，所以只要是其中之一就算是，如果两个都不是，才认为不是在查找宿主的组件
            if (!TextUtils.equals(queryPackageName, hostPackageName) && !TextUtils.equals(queryPackageName, blockPackageName)) {
                return resolveInfos;
            }
            Logger.v("IN PackageManagerHelper, addBlockIntentActivities for : " + intent);
            //插件的manifest有activity，尝试添加进去
            if (resolveInfos == null) {
                resolveInfos = new ArrayList<>();
            }
            ActivityInfo activityInfo;
            for (int i = 0; i < activities.length; i++) {
                activityInfo = activities[i];
                if (TextUtils.equals(queryPackageName, activityInfo.packageName)) {
                    ResolveInfo resolveInfo = new ResolveInfo();
                    resolveInfo.activityInfo = activityInfo;
                    resolveInfo.resolvePackageName = queryPackageName;
                    resolveInfos.add(resolveInfo);
                }
            }
            return resolveInfos;
        } catch (Exception e) {
            Logger.e("IN PackageManagerHelper, addBlockIntentActivities() ERROR : " + e.toString());
            return resolveInfos;
        }
    }

    protected ResolveInfo getBlockResolveService(Intent intent, int flags) {
        try {
            ServiceInfo[] serviceInfos = BlockEnv.sBlockPackageInfo.services;
            if (serviceInfos == null || serviceInfos.length < 0) {
                return null;
            }
            ComponentName component = intent.getComponent();
            if (component == null) return null;
            String queryPackageName = component.getPackageName();
            String queryClassName = component.getClassName();
            String blockPackageName = BlockEnv.sBlockPackageInfo.packageName;
            String hostPackageName = BlockApplication.hostApplicationContext.getPackageName();
            //不知道穿进来的包名是插件的还是宿主的，所以只要是其中之一就算是，如果两个都不是，才认为不是在查找宿主的组件
            if (!TextUtils.equals(queryPackageName, blockPackageName) && !TextUtils.equals(queryPackageName, hostPackageName)) {
                return null;
            }
            Logger.v("IN PackageManagerHelper, getBlockResolveService for : " + intent);
            ServiceInfo serviceInfo;
            for (int i = 0; i < serviceInfos.length; i++) {
                serviceInfo = serviceInfos[i];
                if (TextUtils.equals(queryClassName, serviceInfo.name)) {
                    ResolveInfo resolveInfo = new ResolveInfo();
                    resolveInfo.serviceInfo = serviceInfo;
                    resolveInfo.resolvePackageName = queryPackageName;
                    return resolveInfo;
                }
            }
            return null;
        } catch (Exception e) {
            Logger.e("IN PackageManagerHelper, getBlockResolveService() ERROR : " + e.toString());
            return null;
        }
    }

    protected List<ResolveInfo> addBlockIntentServices(List<ResolveInfo> resolveInfos, Intent intent, int flags) {
        try {
            //插件的service
            ServiceInfo[] serviceInfos = BlockEnv.sBlockPackageInfo.services;
            if (serviceInfos == null || serviceInfos.length < 0) {
                return resolveInfos;
            }

            String queryPackageName = intent.getComponent().getPackageName();
            String queryClassName = intent.getComponent().getClassName();
            String blockPackageName = BlockEnv.sBlockPackageInfo.packageName;
            String hostPackageName = BlockApplication.hostApplicationContext.getPackageName();
            //不知道穿进来的包名是插件的还是宿主的，所以只要是其中之一就算是，如果两个都不是，才认为不是在查找宿主的组件
            if (!TextUtils.equals(queryPackageName, blockPackageName) && !TextUtils.equals(queryPackageName, hostPackageName)) {
                return resolveInfos;
            }
            Logger.v("IN PackageManagerHelper, addBlockIntentServices for : " + intent);
            //插件的manifest有activity，尝试添加进去
            if (resolveInfos == null) {
                resolveInfos = new ArrayList<>();
            }
            ServiceInfo serviceInfo;
            for (int i = 0; i < serviceInfos.length; i++) {
                serviceInfo = serviceInfos[i];
                if (TextUtils.equals(queryClassName, serviceInfo.name)) {
                    ResolveInfo resolveInfo = new ResolveInfo();
                    resolveInfo.serviceInfo = serviceInfo;
                    resolveInfo.resolvePackageName = queryPackageName;
                    resolveInfos.add(resolveInfo);
                }
            }
            return resolveInfos;
        } catch (Exception e) {
            Logger.e("IN PackageManagerHelper, addBlockIntentServices() ERROR : " + e.toString());
            return resolveInfos;
        }
    }
}
