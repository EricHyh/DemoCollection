package com.hyh.common.json.internal;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/21
 */
public interface InstanceCreator<T> {

    T createInstance(Type type);

}
