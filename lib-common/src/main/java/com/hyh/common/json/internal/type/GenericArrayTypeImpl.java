package com.hyh.common.json.internal.type;

import com.hyh.common.json.internal.TypeUtil;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class GenericArrayTypeImpl implements GenericArrayType, Serializable {
    private final Type componentType;

    public GenericArrayTypeImpl(Type componentType) {
        this.componentType = TypeUtil.canonicalize(componentType);
    }

    public Type getGenericComponentType() {
        return componentType;
    }

    @Override public boolean equals(Object o) {
        return o instanceof GenericArrayType
                && TypeUtil.equals(this, (GenericArrayType) o);
    }

    @Override public int hashCode() {
        return componentType.hashCode();
    }

    @Override public String toString() {
        return TypeUtil.typeToString(componentType) + "[]";
    }

    private static final long serialVersionUID = 0;
}
