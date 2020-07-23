package com.hyh.plg.convert;


import android.text.TextUtils;

import com.hyh.plg.utils.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description 用于处理，当接口与实现不是由同一个Classloader加载，而导致的类型转换异常的问题
 * @data 2018/3/14
 */
public class CastHandler implements InvocationHandler {

    private final ClassLoader mProxyClassLoader;

    private final Object mClient;

    private final IProtocolInterface mProtocolInterface;

    private ProtocolInterfaceConverter mResultConverter = new ProtocolInterfaceConverter();

    private MethodParameterConverter mParameterConverter = new MethodParameterConverter();

    CastHandler(ClassLoader proxyClassLoader, Object client, IProtocolInterface protocolInterface) {
        this.mProxyClassLoader = proxyClassLoader;
        this.mClient = client;
        this.mProtocolInterface = protocolInterface;
    }

    //用于保证返回值为基础数据类型时，不返回null
    private final Map<Class, Object> mElementaryDefaultValue = new HashMap<>(8);

    {
        mElementaryDefaultValue.put(byte.class, Byte.valueOf("0"));
        mElementaryDefaultValue.put(short.class, Short.valueOf("0"));
        mElementaryDefaultValue.put(int.class, 0);
        mElementaryDefaultValue.put(long.class, 0L);
        mElementaryDefaultValue.put(float.class, 0.0f);
        mElementaryDefaultValue.put(double.class, 0.0d);
        mElementaryDefaultValue.put(boolean.class, false);
        mElementaryDefaultValue.put(char.class, '\u0000');
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        if (TextUtils.equals(methodName, "produce")) {
            Logger.d("produce");
        }
        if (mClient == null) {
            result = mElementaryDefaultValue.get(returnType);
            Logger.d("impl is null, and method is " + methodName);
        } else {
            try {
                //method = ReflectCache.getMethod(mClient, method, mProtocolInterface);
                method = mProtocolInterface.getProtocolMethod(mClient, method);
                args = mParameterConverter.convert(method, args, mProtocolInterface);
                result = method.invoke(mClient, args);
                if (returnType.equals(void.class)) {
                    return result;
                }
                if (result == null) {
                    return mElementaryDefaultValue.get(returnType);
                }
                if (TextUtils.equals(methodName, "produce")) {
                    Logger.d("produce result1 is " + result + ", classloader is " + result.getClass().getClassLoader());
                }
                result = mResultConverter.convert(mProxyClassLoader, result, mProtocolInterface);
                if (TextUtils.equals(methodName, "produce")) {
                    Logger.d("produce result2 is " + result + ", classloader is " + result.getClass().getClassLoader());
                }
            } catch (Throwable e) {
                result = mElementaryDefaultValue.get(returnType);
                Logger.e("method:" + methodName + " invoke failed", e);
            }
        }
        return result;
    }
}
