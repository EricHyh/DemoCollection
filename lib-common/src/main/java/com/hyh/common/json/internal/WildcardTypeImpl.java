package com.hyh.common.json.internal;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class WildcardTypeImpl implements WildcardType, Serializable {


    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {

    }

    @Override
    public Type[] getUpperBounds() {
        return new Type[0];
    }

    @Override
    public Type[] getLowerBounds() {
        return new Type[0];
    }
}
