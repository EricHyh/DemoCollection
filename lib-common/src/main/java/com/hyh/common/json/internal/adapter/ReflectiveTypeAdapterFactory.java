package com.hyh.common.json.internal.adapter;

import com.hyh.common.json.AJson;
import com.hyh.common.json.TypeToken;
import com.hyh.common.json.annotations.SerializedName;
import com.hyh.common.json.internal.ConstructorConstructor;
import com.hyh.common.json.internal.JSONElement;
import com.hyh.common.json.internal.ObjectConstructor;
import com.hyh.common.json.internal.TypeAdapter;
import com.hyh.common.json.internal.TypeAdapterFactory;
import com.hyh.common.json.internal.TypeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private List<String> getFieldNames(Field f) {
        SerializedName annotation = f.getAnnotation(SerializedName.class);
        if (annotation == null) {
            String name = f.getName();
            return Collections.singletonList(name);
        }

        String serializedName = annotation.value();
        String[] alternates = annotation.alternate();
        if (alternates.length == 0) {
            return Collections.singletonList(serializedName);
        }

        List<String> fieldNames = new ArrayList<>(alternates.length + 1);
        fieldNames.add(serializedName);
        fieldNames.addAll(Arrays.asList(alternates));
        return fieldNames;
    }

    private Map<String, BoundField> getBoundField(AJson context, TypeToken<?> type) {
        Class<?> raw = type.getRawType();
        Map<String, BoundField> result = new LinkedHashMap<String, BoundField>();
        if (raw.isInterface()) {
            return result;
        }
        Type declaredType = type.getType();
        while (raw != Object.class) {
            Field[] fields = raw.getDeclaredFields();
            for (Field field : fields) {
                if (excludeField(field)) continue;

                field.setAccessible(true);
                Type fieldType = TypeUtil.resolve(type.getType(), raw, field.getGenericType());
                List<String> fieldNames = getFieldNames(field);
                BoundField previous = null;
                for (int i = 0; i < fieldNames.size(); ++i) {
                    String name = fieldNames.get(i);

                    BoundField boundField = createBoundField(context, field, name, TypeToken.get(fieldType));
                    BoundField replaced = result.put(name, boundField);
                    if (previous == null) previous = replaced;
                }
                if (previous != null) {
                    throw new IllegalArgumentException(declaredType
                            + " declares multiple JSON fields named " + previous.name);
                }
            }
            type = TypeToken.get(TypeUtil.resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return result;


    }

    private BoundField createBoundField(AJson context, Field field, String name, TypeToken<?> typeToken) {
        return null;
    }

    private boolean excludeField(Field f) {
        return isAnonymousOrLocal(f.getType()) || (f.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) != 0 || f.isSynthetic();
    }

    private boolean isAnonymousOrLocal(Class<?> clazz) {
        return !Enum.class.isAssignableFrom(clazz)
                && (clazz.isAnonymousClass() || clazz.isLocalClass());
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