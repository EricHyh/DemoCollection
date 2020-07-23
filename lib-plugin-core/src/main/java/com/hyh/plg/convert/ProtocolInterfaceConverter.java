package com.hyh.plg.convert;


import java.lang.reflect.Proxy;
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

public class ProtocolInterfaceConverter {

    public Object convert(ClassLoader targetClassLoader, Object object, IProtocolInterface protocolInterface) {
        if (object == null) {
            return null;
        }
        if (object instanceof Object[]) {
            Object[] array = (Object[]) object;
            return convert(targetClassLoader, array, protocolInterface);
        } else if (object instanceof Map) {
            Map map = (Map) object;
            return convert(targetClassLoader, map, protocolInterface);
        } else if (object instanceof Collection) {
            Collection collection = (Collection) object;
            return convert(targetClassLoader, collection, protocolInterface);
        } else {
            Class<?>[] protocolInterfaces = protocolInterface.getProtocolInterfaces(targetClassLoader, object);
            if (protocolInterfaces == null || protocolInterfaces.length == 0) {
                return object;
            }
            return Proxy.newProxyInstance(
                    targetClassLoader,
                    protocolInterfaces,
                    new CastHandler(targetClassLoader, object, protocolInterface));
        }
    }

    public Object[] convert(ClassLoader targetClassLoader, Object[] array, IProtocolInterface protocolInterface) {
        if (array == null || array.length == 0) {
            return array;
        }
        int length = array.length;
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = convert(targetClassLoader, array[i], protocolInterface);
        }
        return result;
    }

    public Collection convert(ClassLoader targetClassLoader, Collection collection, IProtocolInterface protocolInterface) {
        if (!collection.isEmpty()) {
            Iterator iterator = collection.iterator();
            Collection resultCollection = new ArrayList(collection.size());
            while (iterator.hasNext()) {
                Object next = iterator.next();
                next = convert(targetClassLoader, next, protocolInterface);
                resultCollection.add(next);
            }
            return resultCollection;
        }
        return collection;
    }

    public Map convert(ClassLoader targetClassLoader, Map map, IProtocolInterface protocolInterface) {
        if (!map.isEmpty()) {
            Map resultMap = new HashMap(map.size());
            Set<Map.Entry> entries = map.entrySet();
            for (Map.Entry entry : entries) {
                //Object key = convert(targetClassLoader, entry.getKey(), protocolInterface);//Map的Key值不做转换处理
                Object key = entry.getKey();
                Object value = convert(targetClassLoader, entry.getValue(), protocolInterface);
                resultMap.put(key, value);
            }
            return resultMap;
        }
        return map;
    }
}