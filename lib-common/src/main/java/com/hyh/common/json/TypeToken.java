package com.hyh.common.json;

import com.hyh.common.json.internal.TypeUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class TypeToken<T> {

    private final Class<? super T> rawType;
    private final Type type;
    private final int hashCode;

    @SuppressWarnings("unchecked")
    protected TypeToken() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) TypeUtil.getRawType(type);
        this.hashCode = type.hashCode();
    }

    @SuppressWarnings("unchecked")
    private TypeToken(Type type) {
        this.rawType = (Class<? super T>) TypeUtil.getRawType(type);
        this.type = TypeUtil.canonicalize(type);
        this.hashCode = type.hashCode();
    }

    public final Class<? super T> getRawType() {
        return rawType;
    }

    public final Type getType() {
        return type;
    }

    @Override public final int hashCode() {
        return this.hashCode;
    }

    @Override public final boolean equals(Object o) {
        return o instanceof TypeToken<?>
                && TypeUtil.equals(type, ((TypeToken<?>) o).type);
    }

    @Override public final String toString() {
        return TypeUtil.typeToString(type);
    }

    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        assert parameterized != null;
        return TypeUtil.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    public static TypeToken<?> get(Type type) {
        return new TypeToken<>(type);
    }

    public static <T> TypeToken<T> get(Class<T> type) {
        return new TypeToken<T>(type);
    }

    public static TypeToken<?> getParameterized(Type rawType, Type... typeArguments) {
        return new TypeToken<>(TypeUtil.newParameterizedTypeWithOwner(null, rawType, typeArguments));
    }

    public static TypeToken<?> getArray(Type componentType) {
        return new TypeToken<>(TypeUtil.arrayOf(componentType));
    }

}