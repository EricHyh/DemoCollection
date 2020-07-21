package com.hyh.common.json.internal.type;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class GenericArrayTypeImpl implements GenericArrayType, Serializable {


    public GenericArrayTypeImpl(Type type) {

    }

    @Override
    public Type getGenericComponentType() {
        return null;
    }
}
