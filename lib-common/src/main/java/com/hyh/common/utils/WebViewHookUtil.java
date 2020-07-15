/*
 * 文件名: HookUtil.java              
 * 描述: 文件描述                
 * 修改人: [ulegendtimes][tangdongwei]   
 * 修改日期: 2016-9-22 下午6:12:34                 
 * 修改内容: 新增                  
 */
package com.hyh.common.utils;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;

import com.hyh.common.log.Logger;
import com.hyh.common.reflect.Reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WebViewHookUtil {

    /**
     * 是否已经执行hookWebView方法（处理webview在5.1以上系统无法运行问题）
     */
    private static boolean hasHookWebView = false;

    private static boolean hasHookIsDeviceProtectedStorage = false;

    public static void hookWebView() {
        hookWebViewFactory();
        hookIsDeviceProtectedStorageForWebView();
    }

    private static void hookWebViewFactory() {
        if (Process.myUid() != Process.SYSTEM_UID) {
            Logger.d("Process.myUid() != Process.SYSTEM_UID, do not need to hook webview");
            return;
        }
        if (hasHookWebView) {
            Logger.d("webView has been hook!");
            return;
        }
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Method getProviderClassMethod = null;
            Object sProviderInstance = null;

            if (Build.VERSION.SDK_INT >= 26) {
                Logger.d("hookWebViewFactory: Build.VERSION.SDK_INT >= 26");
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);

                Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                Method staticFactory = null;
                staticFactory = providerClass.getMethod("create", delegateClass);

                Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                constructor2.setAccessible(true);

                sProviderInstance = staticFactory.invoke(null, constructor2.newInstance());
            } else if (Build.VERSION.SDK_INT >= 23) {
                Logger.d("hookWebViewFactory: Build.VERSION.SDK_INT >= 23");
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                if (constructor != null) {
                    constructor.setAccessible(true);
                    Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                    constructor2.setAccessible(true);
                    sProviderInstance = constructor.newInstance(constructor2.newInstance());
                }
            } else if (Build.VERSION.SDK_INT == 22) {
                Logger.d("hookWebViewFactory: Build.VERSION.SDK_INT == 22");
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                if (constructor != null) {
                    constructor.setAccessible(true);
                    Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                    constructor2.setAccessible(true);
                    sProviderInstance = constructor.newInstance(constructor2.newInstance());
                }
            } else if (Build.VERSION.SDK_INT == 21) {//Android 21无WebView安全限制
                Logger.d("hookWebViewFactory: Build.VERSION.SDK_INT == 21");
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                sProviderInstance = providerClass.newInstance();
            }
            if (sProviderInstance != null) {
                Logger.d(sProviderInstance.toString());
                Field field = factoryClass.getDeclaredField("sProviderInstance");
                field.setAccessible(true);
                field.set("sProviderInstance", sProviderInstance);
            }
            hasHookWebView = true;
        } catch (Exception e) {
            Logger.e("hookWebView error : ", e);
        }
    }

    private static void hookIsDeviceProtectedStorageForWebView() {
        //如果已经hook过，不再hook
        if (hasHookIsDeviceProtectedStorage) {
            Logger.d("IN WebViewHookUtil,IsDeviceProtectedStorage has been hook! return");
            return;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            Logger.d("IN WebViewHookUtil, hookIsDeviceProtectedStorageForWebView BEGIN...");

            Object currentActivityThread = Reflect.from("android.app.ActivityThread")
                    .method("currentActivityThread")
                    .invoke(null);
            if (currentActivityThread != null) {
                Application application = Reflect.from("android.app.ActivityThread")
                        .method("getApplication", Application.class)
                        .invoke(currentActivityThread);

                boolean isDeviceProtectedStorage = application.isDeviceProtectedStorage();
                Logger.d("IN WebViewHookUtil, hookIsDeviceProtectedStorageForWebView, before hook, isDeviceProtectedStorage=" + isDeviceProtectedStorage);
                if (!isDeviceProtectedStorage) {
                    Logger.d("IN WebViewHookUtil, hookIsDeviceProtectedStorageForWebView, isDeviceProtectedStorage=false, do not have to hook it");
                    hasHookIsDeviceProtectedStorage = true;
                    return;
                }
                Context baseContext = application.getBaseContext();
                Object mFlagsObj = Reflect.from("android.app.ContextImpl")
                        .filed("mFlags")
                        .get(baseContext);
                Logger.d("IN WebViewHookUtil, hookIsDeviceProtectedStorageForWebView, ContextImpl mFlagsObj = " + mFlagsObj);
                if (mFlagsObj != null && mFlagsObj instanceof Integer) {
                    int flags = (int) mFlagsObj;
                    flags &= 0xFFFFFFF7;
                    Reflect.from("android.app.ContextImpl")
                            .filed("mFlags")
                            .set(baseContext, flags);
                    hasHookIsDeviceProtectedStorage = true;
                }
            }
        }
    }
}
