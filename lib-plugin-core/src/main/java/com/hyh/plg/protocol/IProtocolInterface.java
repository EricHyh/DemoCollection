package com.hyh.plg.protocol;

import java.lang.reflect.Method;

/**
 * @author Administrator
 * @description
 * @data 2019/2/28
 */

public interface IProtocolInterface {

    /**
     * 判断指定的“接口class”是不是协议接口
     *
     * @param interfaceClass 指定的“接口class”
     */
    boolean isProtocolInterface(Class<?> interfaceClass);

    Class<?>[] getProtocolInterfaces(ClassLoader targetClassLoader, Object object);

    Method getProtocolMethod(Object target, Method method) throws NoSuchMethodException;

}
