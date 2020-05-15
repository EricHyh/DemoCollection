package com.hyh.plg.activity.transform;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.hyh.plg.activity.ActivityLaunchRecord;
import com.hyh.plg.activity.ActivityProxyImpl;
import com.hyh.plg.activity.IActivity;
import com.hyh.plg.activity.IntentCachePool;
import com.hyh.plg.activity.IntentDataCarrier;
import com.hyh.plg.activity.LaunchMode;
import com.hyh.plg.android.BlockApplication;
import com.hyh.plg.android.BlockEnv;
import com.hyh.plg.android.ProxyActivityInfo;
import com.hyh.plg.android.ams.AMSApi;
import com.hyh.plg.android.ams.ProxyActivityMap;
import com.hyh.plg.exception.ProxyActivityNotFoundException;
import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;
import com.hyh.plg.utils.PackageUtils;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/5/14
 */

public class ProxyActivityTransformation implements Transformation {

    private static final String TRANSFORM_FLAG = "__proxy_activity_transformation__";

    @Override
    public TransformIntent transform(Intent intent) {
        if (intent == null) {
            return null;
        }
        ComponentName componentName = intent.getComponent();
        if (componentName == null || TextUtils.isEmpty(componentName.getClassName())) {
            return null;
        }
        boolean transformFlag = intent.getBooleanExtra(TRANSFORM_FLAG, false);
        Logger.d("ProxyActivityTransformation transform: transformFlag = " + transformFlag);
        if (transformFlag) {
            return null;
        }

        //实现类的包名路径
        String className = componentName.getClassName();

        ProxyActivityMap activityMap = AMSApi.getInstance().getActivityMap(className);
        if (activityMap != null) {
            Class map = activityMap.map;
            if (map != null) {
                if (Reflect.isAssignableFrom(map, IActivity.class)) {
                    return transformToIActivity(intent, activityMap.map.getCanonicalName(), activityMap.mapLaunchMode);
                } else if (Reflect.isAssignableFrom(map, Activity.class)) {
                    return transformToBlockActivity(intent, activityMap.map.getCanonicalName(), activityMap.mapLaunchMode);
                }
            }
        }

        Class componentClass = null;
        try {
            componentClass = Class.forName(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (componentClass == null) {
            return null;
        }

        if (Reflect.isAssignableFrom(componentClass, IActivity.class)) {
            return transformToIActivity(intent, className, intent.getIntExtra(LaunchMode.LAUNCH_MODE, LaunchMode.ANYWAY));
        }

        if (Reflect.isAssignableFrom(componentClass, Activity.class) && ActivityProxyImpl.isBlockActivity(className)) {
            return transformToBlockActivity(intent, className, intent.getIntExtra(LaunchMode.LAUNCH_MODE, LaunchMode.ANYWAY));
        }
        return null;
    }

    private TransformIntent transformToIActivity(Intent intent, String iActivityClassName, int launchMode) {
        //生成key
        String key = String.valueOf(intent.hashCode()) + System.currentTimeMillis();

        Logger.d("IN ProxyActivityTransformation, transformToIActivity(), launchMode=" + launchMode);

        //配置代理Activity的组件
        Intent transformIntent = new Intent();
        int finalLaunchMode;

        Logger.d("IN ProxyActivityTransformation, scheduleLaunchModeLocked(), launchMode = " + launchMode);
        finalLaunchMode = scheduleLaunchModeLocked(transformIntent, launchMode);
        Logger.d("IN ProxyActivityTransformation, scheduleLaunchModeLocked(), component = " + transformIntent.getComponent());

        Logger.d("IN ProxyActivityTransformation, transformToIActivity(), finalLaunchMode=" + finalLaunchMode);

        //以原来的intent配置的flag来启动代理activity
        //注意，在这里put的内容，在fixActivityIntent方法中，需要往原始的intent中也塞进入，否则activity重启的时候，
        transformIntent.setAction("" + System.currentTimeMillis());
        //先set,再add

        int extra_flags = BlockEnv.getProxyActivityIntentFlags(intent, 0);
        Logger.d("IN ProxyActivityTransformation, transformToIActivity(), extra_flags=" + extra_flags);
        if (extra_flags != 0) {
            transformIntent.setFlags(extra_flags);
        }

        transformIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        transformIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //把请求的intent带上一起序列化一把
        transformIntent.putExtras(intent);

        IntentDataCarrier intentDataCarrier = new IntentDataCarrier();
        intentDataCarrier.setBlockClassLoaderHashCode(System.identityHashCode(getClass().getClassLoader()));
        intentDataCarrier.setProxyImplClassPath(iActivityClassName);
        intentDataCarrier.setKeyOfIntent(key);
        intentDataCarrier.setLaunchMode(finalLaunchMode);
        //统一添加，免得忘了
        intentDataCarrier.setFromBlock(true);
        //把关键数据放在代理intent的data中传递，当代理activity起来并且修复序列化后，再把数据从data中取出，放入真实intent的bundle中
        //意思就是代理intent借助data传递数据，拿到真实的intent的还是通过bundle传输数据
        transformIntent.setData(Uri.parse(intentDataCarrier.toString()));

        //把原始的intent放到缓存池中
        IntentCachePool.getInstance().put(key, intent);

        transformIntent.putExtra(TRANSFORM_FLAG, true);
        Logger.d("IN ProxyActivityTransformation, transformToIActivity(), transformIntent : " + transformIntent);
        return new TransformIntent(intent, transformIntent);
    }


    private TransformIntent transformToBlockActivity(Intent intent, String activityClass, int launchMode) {
        //生成key
        String key = String.valueOf(intent.hashCode()) + System.currentTimeMillis();

        Logger.d("IN ProxyActivityTransformation, transformToBlockActivity(), launchMode=" + launchMode);

        //配置代理Activity的组件
        Intent transformIntent = new Intent();
        int finalLaunchMode;

        Logger.d("IN ProxyActivityTransformation, scheduleLaunchModeLocked(), launchMode = " + launchMode);
        finalLaunchMode = scheduleLaunchModeLocked(transformIntent, launchMode);
        Logger.d("IN ProxyActivityTransformation, scheduleLaunchModeLocked(), component = " + transformIntent.getComponent());

        //以原来的intent配置的flag来启动代理activity
        //注意，在这里put的内容，在fixActivityIntent方法中，需要往原始的intent中也塞进入，否则activity重启的时候，
        transformIntent.setAction("" + System.currentTimeMillis());
        //先set,再add

        int extra_flags = BlockEnv.getProxyActivityIntentFlags(intent, 0);
        Logger.d("IN ProxyActivityTransformation, transformToBlockActivity(), extra_flags=" + extra_flags);
        if (extra_flags != 0) {
            transformIntent.setFlags(extra_flags);
        }

        transformIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        transformIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //把请求的intent带上一起序列化一把
        transformIntent.putExtras(intent);

        IntentDataCarrier intentDataCarrier = new IntentDataCarrier();
        intentDataCarrier.setBlockClassLoaderHashCode(System.identityHashCode(getClass().getClassLoader()));
        intentDataCarrier.setBlockActivityClassPath(activityClass);
        intentDataCarrier.setKeyOfIntent(key);
        intentDataCarrier.setOpenBlockActivity(true);
        intentDataCarrier.setLaunchMode(finalLaunchMode);
        //统一添加，免得忘了
        intentDataCarrier.setFromBlock(true);
        //把关键数据放在代理intent的data中传递，当代理activity起来并且修复序列化后，再把数据从data中取出，放入真实intent的bundle中
        //意思就是代理intent借助data传递数据，拿到真实的intent的还是通过bundle传输数据
        transformIntent.setData(Uri.parse(intentDataCarrier.toString()));

        //把原始的intent放到缓存池中
        IntentCachePool.getInstance().put(key, intent);

        transformIntent.putExtra(TRANSFORM_FLAG, true);
        Logger.d("IN ProxyActivityTransformation, transformToBlockActivity(), transformIntent : " + transformIntent);
        return new TransformIntent(intent, transformIntent);
    }

    private static int scheduleLaunchModeLocked(Intent transformIntent, int launchMode) throws ProxyActivityNotFoundException {
        synchronized (ActivityLaunchRecord.sActivityLaunchRecordLock) {
            switch (launchMode) {
                default:
                case LaunchMode.ANYWAY: {
                    //如果不指定，优先采用标准模式的代理，再考虑SingleInstance的
                    boolean schedule = scheduleByStandard(transformIntent);
                    if (schedule) {
                        return LaunchMode.STANDARD;
                    }
                    schedule = scheduleBySingleInstance(transformIntent);
                    if (schedule) {
                        return LaunchMode.SINGLE_INSTANCE;
                    }
                    String message = "LaunchMode.ANYWAY: can not find any exist proxy activity, ProxyActivityInfo = "
                            + BlockEnv.sProxyActivityInfo;
                    throw new ProxyActivityNotFoundException(message);
                }
                case LaunchMode.STANDARD: {
                    boolean schedule = scheduleByStandard(transformIntent);
                    if (schedule) {
                        return LaunchMode.STANDARD;
                    }
                    String message = "LaunchMode.STANDARD: can not find any exist proxy activity, ProxyActivityInfo = "
                            + BlockEnv.sProxyActivityInfo;
                    throw new ProxyActivityNotFoundException(message);
                }
                case LaunchMode.SINGLE_INSTANCE: {
                    boolean schedule = scheduleBySingleInstance(transformIntent);
                    if (schedule) {
                        return LaunchMode.SINGLE_INSTANCE;
                    }
                    String message = "LaunchMode.SINGLE_INSTANCE: can not find any exist proxy activity, ProxyActivityInfo = "
                            + BlockEnv.sProxyActivityInfo;
                    throw new ProxyActivityNotFoundException(message);
                }
                case LaunchMode.PRIOR_SINGLE_INSTANCE: {
                    boolean schedule = scheduleBySingleInstance(transformIntent);
                    if (schedule) {
                        return LaunchMode.SINGLE_INSTANCE;
                    }
                    schedule = scheduleByStandard(transformIntent);
                    if (schedule) {
                        return LaunchMode.STANDARD;
                    }
                    String message = "LaunchMode.PRIOR_SINGLE_INSTANCE: can not find any exist proxy activity, ProxyActivityInfo = "
                            + BlockEnv.sProxyActivityInfo;
                    throw new ProxyActivityNotFoundException(message);
                }
            }
        }
    }

    private static boolean scheduleByStandard(Intent transformIntent) {
        List<ProxyActivityInfo.ProxyActivity> standardProxyActivities = BlockEnv.sProxyActivityInfo.standardProxyActivities;
        return !standardProxyActivities.isEmpty() && scheduleByProxyActivities(transformIntent, standardProxyActivities);
    }

    private static boolean scheduleBySingleInstance(Intent transformIntent) {
        List<ProxyActivityInfo.ProxyActivity> singleInstanceProxyActivities = BlockEnv.sProxyActivityInfo.singleInstanceProxyActivities;
        return !singleInstanceProxyActivities.isEmpty() && scheduleByProxyActivities(transformIntent, singleInstanceProxyActivities);
    }

    private static boolean scheduleByProxyActivities(Intent transformIntent, List<ProxyActivityInfo.ProxyActivity> proxyActivities) {
        for (ProxyActivityInfo.ProxyActivity proxyActivity : proxyActivities) {
            if (proxyActivity.isManifestRegistered) {
                transformIntent.setComponent(
                        new ComponentName(BlockApplication.hostApplicationContext.getPackageName(), proxyActivity.activityName));
                return true;
            } else if (PackageUtils.isActivityExist(BlockApplication.hostApplicationContext, proxyActivity.activityName)) {
                proxyActivity.isManifestRegistered = true;
                transformIntent.setComponent(
                        new ComponentName(BlockApplication.hostApplicationContext.getPackageName(), proxyActivity.activityName));
                return true;
            }
        }
        return false;
    }
}