package com.hyh.common.json.internal.type;

import com.hyh.common.json.internal.TypeUtil;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class ParameterizedTypeImpl implements ParameterizedType, Serializable {

    private final Type ownerType;
    private final Type rawType;
    private final Type[] typeArguments;

    public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
        // require an owner type if the raw type needs it
        if (rawType instanceof Class<?>) {
            Class<?> rawTypeAsClass = (Class<?>) rawType;
            boolean isStaticOrTopLevelClass = Modifier.isStatic(rawTypeAsClass.getModifiers())
                    || rawTypeAsClass.getEnclosingClass() == null;
            TypeUtil.checkArgument(ownerType != null || isStaticOrTopLevelClass);
        }

        this.ownerType = ownerType == null ? null : TypeUtil.canonicalize(ownerType);
        this.rawType = TypeUtil.canonicalize(rawType);
        this.typeArguments = typeArguments.clone();
        for (int t = 0; t < this.typeArguments.length; t++) {
            TypeUtil.checkNotNull(this.typeArguments[t]);
            TypeUtil.checkNotPrimitive(this.typeArguments[t]);
            this.typeArguments[t] = TypeUtil.canonicalize(this.typeArguments[t]);
        }
    }

    public Type[] getActualTypeArguments() {
        return typeArguments.clone();
    }

    public Type getRawType() {
        return rawType;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ParameterizedType
                && TypeUtil.equals(this, (ParameterizedType) other);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(typeArguments)
                ^ rawType.hashCode()
                ^ TypeUtil.hashCodeOrZero(ownerType);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(30 * (typeArguments.length + 1));
        stringBuilder.append(TypeUtil.typeToString(rawType));

        if (typeArguments.length == 0) {
            return stringBuilder.toString();
        }

        stringBuilder.append("<").append(TypeUtil.typeToString(typeArguments[0]));
        for (int i = 1; i < typeArguments.length; i++) {
            stringBuilder.append(", ").append(TypeUtil.typeToString(typeArguments[i]));
        }
        return stringBuilder.append(">").toString();
    }

    private static final long serialVersionUID = 0;
}
