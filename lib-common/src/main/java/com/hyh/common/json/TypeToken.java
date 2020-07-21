package com.hyh.common.json;

import com.hyh.common.json.internal.TypeUtil;

import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class TypeToken<T> {

    private final Class<? super T> rawType;
    private final Type type;

    public TypeToken(Type type) {
        this.rawType = TypeUtil.getRawType(type);
        this.type = TypeUtil.canonicalize(type);
    }

    public final Class<? super T> getRawType() {
        return rawType;
    }


    public final Type getType() {
        return type;
    }


    public static TypeToken<?> get(Type type) {
        return new TypeToken<Object>(type);
    }

    /**
     * Gets type literal for the given {@code Class} instance.
     */
    /*public static <T> TypeToken<T> get(Class<T> type) {
        return new TypeToken<T>(type);
    }

    *//**
     * Gets type literal for the parameterized type represented by applying {@code typeArguments} to
     * {@code rawType}.
     *//*
    public static TypeToken<?> getParameterized(Type rawType, Type... typeArguments) {
        return new TypeToken<Object>($Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments));
    }

    *//**
     * Gets type literal for the array type whose elements are all instances of {@code componentType}.
     *//*
    public static TypeToken<?> getArray(Type componentType) {
        return new TypeToken<Object>($Gson$Types.arrayOf(componentType));
    }*/
}
