package com.hyh.plg.protocol;

import java.lang.reflect.Method;

/**
 * @author Administrator
 * @description
 * @data 2019/2/28
 */

public class EmptyProtocolInterface implements IProtocolInterface {

    @Override
    public boolean isProtocolInterface(Class<?> interfaceClass) {
        return false;
    }

    @Override
    public Class<?>[] getProtocolInterfaces(ClassLoader targetClassLoader, Object object) {
        return new Class[0];
    }

    @Override
    public Method getProtocolMethod(Object target, Method method) throws NoSuchMethodException {
        return null;
    }
}