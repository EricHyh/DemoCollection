package com.hyh.plg.convert;


import com.hyh.plg.reflect.Reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2019/3/7
 */

public class MethodParameterConverter {

    private ProtocolInterfaceConverter mConverter = new ProtocolInterfaceConverter();

    Object[] convert(Method method, Object[] parameters, IProtocolInterface protocolInterface) {
        if (parameters == null || parameters.length <= 0) {
            return parameters;
        }
        Type[] parameterTypes = method.getGenericParameterTypes();
        int length = parameters.length;
        Object[] resultParameters = new Object[length];
        for (int index = 0; index < length; index++) {
            Object parameter = parameters[index];
            if (parameter == null) {
                continue;
            }
            Type parameterType = parameterTypes[index];
            resultParameters[index] = convert(parameter, parameterType, protocolInterface);
        }
        return resultParameters;
    }


    private Object convert(Object parameter, Type parameterType, IProtocolInterface protocolInterface) {
        if (parameter instanceof Object[] && parameterType instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) parameterType).getGenericComponentType();
            Object[] array = (Object[]) parameter;
            if (array.length > 0) {
                return convertArray(array, componentType, protocolInterface);
            }
        } else if (parameter instanceof Collection && parameterType instanceof ParameterizedType) {
            Collection collection = (Collection) parameter;
            if (collection.size() > 0) {
                Type componentType = ((ParameterizedType) parameterType).getActualTypeArguments()[0];
                return convertCollection((Collection) parameter, componentType, protocolInterface);
            }
        } else if (parameter instanceof Map && parameterType instanceof ParameterizedType) {
            Map map = (Map) parameter;
            if (map.size() > 0) {
                Type valueType = ((ParameterizedType) parameterType).getActualTypeArguments()[1];
                return convertMap((Map) parameter, valueType, protocolInterface);
            }
        } else if (parameterType instanceof Class) {
            return convertObject(parameter, (Class) parameterType, protocolInterface);
        }
        return parameter;
    }


    private Object convertObject(Object parameter, Class parameterType, IProtocolInterface protocolInterface) {
        if (isNeedConvert(parameter, parameterType, protocolInterface)) {
            return mConverter.convert(parameterType.getClassLoader(), parameter, protocolInterface);
        }
        return parameter;
    }


    private Object[] convertArray(Object[] arrayParameter, Type componentType, IProtocolInterface protocolInterface) {
        if (componentType instanceof Class) {
            Class cls = (Class) componentType;
            if (isNeedConvert(arrayParameter[0], cls, protocolInterface)) {
                int arrayLength = arrayParameter.length;
                Object[] result = (Object[]) Array.newInstance(cls, arrayLength);
                for (int index = 0; index < arrayLength; index++) {
                    result[index] = mConverter.convert(cls.getClassLoader(), arrayParameter[index], protocolInterface);
                }
                return result;
            }
        }
        return arrayParameter;
    }

    private Collection convertCollection(Collection collectionParameter, Type componentType, IProtocolInterface protocolInterface) {
        if (componentType instanceof Class) {
            Class cls = (Class) componentType;
            Collection resultCollection = new ArrayList(collectionParameter.size());
            Iterator iterator = collectionParameter.iterator();
            boolean isNeedConvert = false;
            do {
                Object next = iterator.next();
                if (isNeedConvert || isNeedConvert(next, cls, protocolInterface)) {
                    resultCollection.add(mConverter.convert(cls.getClassLoader(), next, protocolInterface));
                    isNeedConvert = true;
                } else {
                    break;
                }
            } while (iterator.hasNext());
            if (isNeedConvert) {
                return resultCollection;
            }
        }
        return collectionParameter;
    }

    private Map convertMap(Map parameter, Type valueType, IProtocolInterface protocolInterface) {
        if (valueType instanceof Class) {
            Class cls = (Class) valueType;
            Map resultMap = new HashMap(parameter.size());
            Set<Map.Entry> set = parameter.entrySet();
            boolean isNeedConvert = false;
            for (Map.Entry entry : set) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (isNeedConvert || isNeedConvert(value, cls, protocolInterface)) {
                    resultMap.put(key, mConverter.convert(cls.getClassLoader(), value, protocolInterface));
                    isNeedConvert = true;
                }
            }
            if (isNeedConvert) {
                return resultMap;
            }
        }
        return null;
    }

    private boolean isNeedConvert(Object original, Class targetClass, IProtocolInterface protocolInterface) {
        return targetClass.isInterface()
                && !Reflect.isChildClassLoader(original.getClass().getClassLoader(), targetClass.getClassLoader())
                && protocolInterface.isProtocolInterface(targetClass);
    }
}