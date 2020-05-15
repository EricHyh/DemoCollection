package com.hyh.plg.android.pms;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.hyh.plg.android.BlockApplication;
import com.hyh.plg.utils.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Created by tangdongwei on 2018/12/7.
 */
class BlockPackageManagerDynaProxy {

    protected PackageManager create(Application application, PackageManager realPackageManager) {
        try {
            Class<?> ContextImplClass = BlockPackageManagerDynaProxy.class.getClassLoader().loadClass("android.app.ContextImpl");
            Class<?> IPackageManagerClass = BlockPackageManagerDynaProxy.class.getClassLoader().loadClass("android.content.pm.IPackageManager");

            Class<?> ApplicationPackageManagerClass = BlockPackageManagerDynaProxy.class.getClassLoader().loadClass("android.app.ApplicationPackageManager");
            Constructor<?> ApplicationPackageManagerConstructor = ApplicationPackageManagerClass.getDeclaredConstructor(ContextImplClass, IPackageManagerClass);
            ApplicationPackageManagerConstructor.setAccessible(true);

            Method getImpl = ContextImplClass.getDeclaredMethod("getImpl", Context.class);
            getImpl.setAccessible(true);
            Object contextImpl = getImpl.invoke(null, (Context) application);

            Object realIPackageManager = getRealIPackageManager(realPackageManager, ApplicationPackageManagerClass);
            Object blockApplicationPM = ApplicationPackageManagerConstructor.newInstance(contextImpl,
                      Proxy.newProxyInstance(BlockPackageManagerDynaProxy.class.getClassLoader(), new Class[]{IPackageManagerClass}, new IPackageManagerHandler(realIPackageManager)));
            return (PackageManager) blockApplicationPM;
        } catch (Exception e) {
            Logger.e("IN BlockPackageManagerDynaProxy, create ERROR : " + e.toString());
            return null;
        }
    }

    private Object getRealIPackageManager(PackageManager realPackageManager, Class ApplicationPackageManagerClass) throws Exception {
        Field mPM = ApplicationPackageManagerClass.getDeclaredField("mPM");
        mPM.setAccessible(true);
        return mPM.get(realPackageManager);
    }

    private class IPackageManagerHandler implements InvocationHandler {

        private static final String METHOD_getPackageInfo = "getPackageInfo";
        private static final String METHOD_getApplicationInfo = "getApplicationInfo";
        private static final String METHOD_resolveActivity_Service = "resolveIntent";
        private static final String METHOD_queryIntentActivities = "queryIntentActivities";
        private static final String METHOD_queryIntentServices = "queryIntentServices";

        Object mRealIPM;

        public IPackageManagerHandler(Object realPackageManager) {
            this.mRealIPM = realPackageManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method == null ? "" : method.getName();
            switch (methodName) {
                case METHOD_getPackageInfo: {
                    return getPackageInfo(proxy, method, args);
                }
                case METHOD_getApplicationInfo: {
                    return getApplicationInfo(proxy, method, args);
                }
                case METHOD_resolveActivity_Service: {
                    return resolveIntent(proxy, method, args);
                }
                case METHOD_queryIntentActivities: {
                    return queryIntentActivities(proxy, method, args);
                }
                case METHOD_queryIntentServices: {
                    return queryIntentServices(proxy, method, args);
                }
                default: {
                    return method.invoke(mRealIPM, args);
                }
            }
        }

        private Object getPackageInfo(Object proxy, Method method, Object[] args) throws Throwable {
            String packageName = "";
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof String) {
                        packageName = (String) arg;
                        break;
                    }
                }
            }
            Logger.v("IN IPackageManagerHandler, invoke : " + METHOD_getPackageInfo + " , packageName=" + packageName);
            Object result = method.invoke(mRealIPM, args);
            if (result != null && result instanceof PackageInfo && TextUtils.equals(packageName, BlockApplication.hostApplicationContext.getPackageName())) {
                PackageInfo packageInfo = (PackageInfo) result;
                PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                packageInfo.applicationInfo = packageManagerHelper.makeBlockApplicationInfoBaseOnHost(packageInfo.applicationInfo, true);
                return packageInfo;
            } else {
                return result;
            }
        }

        private Object getApplicationInfo(Object proxy, Method method, Object[] args) throws Throwable {
            String packageName = "";
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof String) {
                        packageName = (String) arg;
                        break;
                    }
                }
            }
            Logger.v("IN IPackageManagerHandler, invoke : " + METHOD_getApplicationInfo + " , packageName=" + packageName);
            Object result = method.invoke(mRealIPM, args);
            if (result != null && result instanceof ApplicationInfo && TextUtils.equals(packageName, BlockApplication.hostApplicationContext.getPackageName())) {
                ApplicationInfo applicationInfo = (ApplicationInfo) result;
                PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                return packageManagerHelper.makeBlockApplicationInfoBaseOnHost(applicationInfo, true);
            } else {
                return result;
            }
        }

        private Object resolveIntent(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(mRealIPM, args);
            if (result != null) {
                return result;
            } else {
                Intent intent = null;
                if (args != null) {
                    for (Object arg : args) {
                        if (arg instanceof Intent) {
                            intent = (Intent) arg;
                            break;
                        }
                    }
                }
                if (!PackageManagerHelper.checkIntentComponent(intent)) {
                    return null;
                }
                Logger.v("IN IPackageManagerHandler, invoke : " + METHOD_resolveActivity_Service + " , intent=" + intent);
                PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                return packageManagerHelper.getBlockResolveActivityOrService(intent);
            }
        }

        private Object queryIntentActivities(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(mRealIPM, args);
            Intent intent = null;
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof Intent) {
                        intent = (Intent) arg;
                        break;
                    }
                }
            }
            Logger.v("IN IPackageManagerHandler, invoke : " + METHOD_queryIntentActivities + " , intent=" + intent);
            //检查一下intent的合法性
            if (!PackageManagerHelper.checkIntentComponent(intent)) {
                return result;
            }
            //版本小于Android 5.0
            //  List<ResolveInfo> queryIntentActivities(in Intent intent, String resolvedType, int flags, int userId);
            if (result != null && result instanceof List) {
                try {
                    //原来的结果
                    List data = (List) result;
                    PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                    //add插件后的结果
                    data = packageManagerHelper.addBlockIntentActivities(data, intent, 0);
                    return data;
                } catch (Exception e) {
                    Logger.w("IN IPackageManagerHandler, invoke : " + METHOD_queryIntentActivities + " ERROR : " + e.toString());
                    return result;
                }
            }
            //版本大于android 5.0
            //  ParceledListSlice queryIntentActivities(in Intent intent, String resolvedType, int flags, int userId);
            else if (result != null && ParceledListSliceCompat.isParceledListSlice(result)) {
                try {
                    Method getListMethod = ParceledListSliceCompat.Class().getMethod("getList");
                    //原来的结果
                    List<ResolveInfo> data = (List) getListMethod.invoke(result);
                    PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                    //add插件后的结果
                    data = packageManagerHelper.addBlockIntentActivities(data, intent, 0);
                    return result;
                } catch (Exception e) {
                    Logger.w("IN IPackageManagerHandler, invoke : " + METHOD_queryIntentActivities + " ERROR : " + e.toString());
                    return result;
                }
            } else if (result == null) {
                Class<?> returnType = method.getReturnType();
                if (returnType != null) {
                    // 如果返回值是List类型
                    if (TextUtils.equals(returnType.getName(), List.class.getName())) {
                        PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                        List<ResolveInfo> resolveInfos = packageManagerHelper.addBlockIntentActivities(null, intent, 0);
                        return resolveInfos;
                    }
                    //如果返回值是ParceledListSliceCompat类型
                    else if (TextUtils.equals(returnType.getName(), ParceledListSliceCompat.Class().getName())) {
                        // 让源码上看，这种情况不存在，因为BaseParceledListSlice里会给mList赋值，所以必定不为null
                        return result;
                    }
                }
                return result;
            } else {
                return result;
            }
        }

        private Object queryIntentServices(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(mRealIPM, args);
            Intent intent = null;
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof Intent) {
                        intent = (Intent) arg;
                        break;
                    }
                }
            }
            Logger.v("IN IPackageManagerHandler, invoke : " + METHOD_queryIntentServices + " , intent=" + intent);
            //检查一下intent的合法性
            if (!PackageManagerHelper.checkIntentComponent(intent)) {
                return result;
            }
            //版本小于Android 5.0
            //  List<ResolveInfo> queryIntentActivities(in Intent intent, String resolvedType, int flags, int userId);
            if (result != null && result instanceof List) {
                try {
                    //原来的结果
                    List data = (List) result;
                    PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                    //add插件后的结果
                    data = packageManagerHelper.addBlockIntentServices(data, intent, 0);
                    return data;
                } catch (Exception e) {
                    Logger.w("IN IPackageManagerHandler, invoke : " + METHOD_queryIntentServices + " ERROR : " + e.toString());
                    return result;
                }
            }
            //版本大于android 5.0
            //  ParceledListSlice queryIntentActivities(in Intent intent, String resolvedType, int flags, int userId);
            else if (result != null && ParceledListSliceCompat.isParceledListSlice(result)) {
                try {
                    Method getListMethod = ParceledListSliceCompat.Class().getMethod("getList");
                    //原来的结果
                    List<ResolveInfo> data = (List) getListMethod.invoke(result);
                    PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                    //add插件后的结果
                    data = packageManagerHelper.addBlockIntentServices(data, intent, 0);
                    return result;
                } catch (Exception e) {
                    Logger.w("IN IPackageManagerHandler, invoke : " + METHOD_queryIntentServices + " ERROR : " + e.toString());
                    return result;
                }
            } else if (result == null) {
                Class<?> returnType = method.getReturnType();
                if (returnType != null) {
                    // 如果返回值是List类型
                    if (TextUtils.equals(returnType.getName(), List.class.getName())) {
                        PackageManagerHelper packageManagerHelper = new PackageManagerHelper();
                        List<ResolveInfo> resolveInfos = packageManagerHelper.addBlockIntentServices(null, intent, 0);
                        return resolveInfos;
                    }
                    //如果返回值是ParceledListSliceCompat类型
                    else if (TextUtils.equals(returnType.getName(), ParceledListSliceCompat.Class().getName())) {
                        // 让源码上看，这种情况不存在，因为BaseParceledListSlice里会给mList赋值，所以必定不为null
                        return result;
                    }
                }
                return result;
            } else {
                return result;
            }
        }
    }


}
