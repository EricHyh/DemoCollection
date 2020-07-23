package com.hyh.plg.convert;

import com.hyh.plg.reflect.Reflect;
import com.hyh.plg.utils.Logger;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2019/2/28
 */

public class BaseProtocolInterface implements IProtocolInterface {

    private final Map<String, Class<?>[]> mProtocolInterfaceMap = new HashMap<>();
    private final Map<Class<?>, Map<Method, Method>> mProtocolMethodMap = new HashMap<>();
    private final List<String> mProtocolInterfaceNames;

    public BaseProtocolInterface(List<String> protocolInterfaceNames) {
        this.mProtocolInterfaceNames = protocolInterfaceNames;
    }

    @Override
    public boolean isProtocolInterface(Class<?> interfaceClass) {
        String name = interfaceClass.getName();
        return mProtocolInterfaceNames.contains(name);
    }

    @Override
    public Class<?>[] getProtocolInterfaces(ClassLoader targetClassLoader, Object object) {
        if (object == null) {
            return null;
        }
        Class<?> cls = object.getClass();
        String key = generateKey(targetClassLoader, cls);
        Class<?>[] interfaces = mProtocolInterfaceMap.get(key);
        if (interfaces != null) {
            return interfaces;
        }
        Collection<Class<?>> protocolInterfaceCollection = new HashSet<>();
        do {
            Class<?>[] clsInterfaces = cls.getInterfaces();
            selectProtocolInterface(targetClassLoader, clsInterfaces, protocolInterfaceCollection);
            cls = cls.getSuperclass();
        } while (cls != null);
        Class<?>[] protocolInterfaces = protocolInterfaceCollection.toArray(new Class<?>[protocolInterfaceCollection.size()]);
        mProtocolInterfaceMap.put(key, protocolInterfaces);
        return protocolInterfaces;
    }

    @Override
    public Method getProtocolMethod(Object target, Method method) throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        if (targetClass.equals(method.getDeclaringClass())) {
            return method;
        }
        Map<Method, Method> methodMethodMap = mProtocolMethodMap.get(targetClass);
        if (methodMethodMap != null) {
            Method protocolMethod = methodMethodMap.get(method);
            if (protocolMethod != null) {
                return protocolMethod;
            }
        }
        ClassLoader targetClassLoader = targetClass.getClassLoader();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        parameterTypes = castTypeToTargetClassLoader(targetClassLoader, parameterTypes);
        Method protocolMethod = targetClass.getMethod(methodName, parameterTypes);
        if (methodMethodMap != null) {
            methodMethodMap.put(method, protocolMethod);
        } else {
            methodMethodMap = new HashMap<>();
            methodMethodMap.put(method, protocolMethod);
            mProtocolMethodMap.put(targetClass, methodMethodMap);
        }
        return protocolMethod;
    }

    private Class<?>[] castTypeToTargetClassLoader(ClassLoader targetClassLoader, Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return parameterTypes;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            ClassLoader classLoader = parameterType.getClassLoader();
            if (classLoader == null
                    || !parameterType.isInterface()
                    || targetClassLoader == classLoader
                    || Reflect.isChildClassLoader(targetClassLoader, classLoader)
                    || !isProtocolInterface(parameterType)) {
                continue;
            }
            Class cls = loadTargetProtocolClass(targetClassLoader, parameterType.getName());
            if (cls != null) {
                parameterTypes[i] = cls;
            }
        }
        return parameterTypes;
    }

    private String generateKey(ClassLoader classLoader, Class cls) {
        String key = "";
        int hashCode = System.identityHashCode(classLoader);
        key += hashCode;
        key += "-";
        key += System.identityHashCode(cls);
        return key;
    }

    private void selectProtocolInterface(ClassLoader targetClassLoader, Class<?>[] clsInterfaces, Collection<Class<?>> clsInterfaceCollection) {
        if (clsInterfaces == null || clsInterfaces.length <= 0) return;
        for (Class<?> clsInterface : clsInterfaces) {
            if (isProtocolInterface(clsInterface)) {
                ClassLoader classLoader = clsInterface.getClassLoader();
                if (classLoader == targetClassLoader || Reflect.isChildClassLoader(targetClassLoader, classLoader)) {
                    clsInterfaceCollection.add(clsInterface);
                } else {
                    clsInterfaceCollection.add(loadTargetProtocolClass(targetClassLoader, clsInterface.getName()));
                }
            }
        }
    }

    /**
     * 加载目标指定ClassLoader的Class对象
     *
     * @param targetClassLoader 指定ClassLoader
     * @param protocolClassName 要加载的类路径
     * @return
     */
    protected Class loadTargetProtocolClass(ClassLoader targetClassLoader, String protocolClassName) {
        Class cls = null;
        try {
            cls = targetClassLoader.loadClass(protocolClassName);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.w("loadTargetProtocolClass failed: targetClassLoader is " + targetClassLoader
                    + ", protocolClassName is " + protocolClassName, e);
        }
        return cls;
    }
}