package com.hyh.common.json.internal;

import com.hyh.common.json.internal.type.GenericArrayTypeImpl;
import com.hyh.common.json.internal.type.ParameterizedTypeImpl;
import com.hyh.common.json.internal.type.WildcardTypeImpl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class TypeUtil {

    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(),
                    p.getRawType(), p.getActualTypeArguments());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());
        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
        } else {
            // type is either serializable as-is or unsupported
            return type;
        }
    }

    static boolean isPrimitive(Object obj) {
        return false;
    }

    static boolean isArray(Object obj) {
        return false;
    }

    public static boolean isCollection(Object value) {
        return false;
    }

    public static boolean isMap(Object value) {
        return false;
    }

    public static <T> Class<? super T> getRawType(Type type) {
        return null;
    }
}
