package com.hyh.common.json.internal.adapter;

import com.hyh.common.json.AJson;
import com.hyh.common.json.TypeToken;
import com.hyh.common.json.internal.ConstructorConstructor;
import com.hyh.common.json.internal.JSONElement;
import com.hyh.common.json.internal.ObjectConstructor;
import com.hyh.common.json.internal.TypeAdapter;
import com.hyh.common.json.internal.TypeAdapterFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author Administrator
 * @description
 * @data 2020/7/21
 */
public class ReflectiveTypeAdapterFactory implements TypeAdapterFactory {

    private final ConstructorConstructor constructor;

    public ReflectiveTypeAdapterFactory(ConstructorConstructor constructor) {
        this.constructor = constructor;
    }

    @Override
    public <T> TypeAdapter<T> create(AJson aJson, TypeToken<T> type) {
        ObjectConstructor<T> constructor = this.constructor.get(type);
        return new Adapter<>(constructor);
    }

    private final static class Adapter<T> implements TypeAdapter<T> {

        private final ObjectConstructor<T> constructor;

        Adapter(ObjectConstructor<T> constructor) {
            this.constructor = constructor;
        }

        @Override
        public T read(JSONElement in) {

            return null;
        }

        @Override
        public JSONElement write(T value) {
            return null;
        }
    }


    private static class BoundField {

        private String name;
        private Type type;
        private Field field;
        private TypeAdapter<?> typeAdapter;

    }
}