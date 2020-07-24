package com.hyh.plg.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class IActivityManagerProxyForService {

    //用于保证返回值为基础数据类型时，不返回null
    private static final Map<Class, Object> mElementaryDefaultValue;

    static {
        mElementaryDefaultValue = new HashMap<>(8);
        mElementaryDefaultValue.put(byte.class, Byte.valueOf("0"));
        mElementaryDefaultValue.put(short.class, Short.valueOf("0"));
        mElementaryDefaultValue.put(int.class, 0);
        mElementaryDefaultValue.put(long.class, 0L);
        mElementaryDefaultValue.put(float.class, 0.0f);
        mElementaryDefaultValue.put(double.class, 0.0d);
        mElementaryDefaultValue.put(boolean.class, false);
        mElementaryDefaultValue.put(char.class, '\u0000');
    }

    public static Object create(Context context) {
        try {
            Class IActivityManagerClass = IActivityManagerProxyForService.class.getClassLoader().loadClass("android.app.IActivityManager");
            return Proxy.newProxyInstance(IActivityManagerProxyForService.class.getClassLoader(), new Class[]{IActivityManagerClass}, new IActivityManagerHandleForService(context));
        } catch (Exception e) {
            Logger.e("IN IActivityManagerProxyForService, create(), ERROR : " + e.toString());
            return null;
        }
    }

    private static class IActivityManagerHandleForService implements InvocationHandler {

        private Context context;

        IActivityManagerHandleForService(Context context) {
            this.context = context;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Logger.v("IN IActivityManagerHandleForService, invoke() : " + method);
            switch (method.getName()) {
                /*
                mActivityManager.stopServiceToken(new ComponentName(this, mClassName), mToken, startId);
                 */
                case "stopServiceToken": {
                    ComponentName componentName = null;
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null && args[i] instanceof ComponentName) {
                            componentName = (ComponentName) args[i];
                        }
                    }
                    if (componentName != null) {
                        Intent intent = new Intent();
                        intent.setComponent(componentName);
                        BlockServiceServer.getInstance(context).stopService(intent);
                    }
                    break;
                }
                /*
                mActivityManager.setServiceForeground(new ComponentName(this, mClassName), mToken, id, notification, 0);
                 */
                case "setServiceForeground": {
                    break;
                }
            }
            return Reflect.getDefaultValue(method.getReturnType());
        }
    }
}
