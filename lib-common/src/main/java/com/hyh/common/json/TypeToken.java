package com.hyh.common.json;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class TypeToken<T> {

    private final Class<? super T> rawType;
    private final Type type;

    public TypeToken() {

    }

    public final Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Gets underlying {@code Type} instance.
     */
    public final Type getType() {
        return type;
    }
}
