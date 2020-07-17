package com.hyh.common.json.internal;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class ParameterizedTypeImpl implements ParameterizedType, Serializable {


    public ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] actualTypeArguments) {

    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[0];
    }

    @Override
    public Type getRawType() {
        return null;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
