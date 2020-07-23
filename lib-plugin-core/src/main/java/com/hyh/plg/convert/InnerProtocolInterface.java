package com.hyh.plg.convert;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 * @description
 * @data 2019/2/28
 */

public class InnerProtocolInterface extends BaseProtocolInterface {

    private final Map<String, String> mHostProtocolInterfaceMap;
    private final Map<String, String> mBlockProtocolInterfaceMap;
    private final ClassLoader mHostClassLoader;

    public InnerProtocolInterface(ClassLoader hostClassLoader, List<String> protocolInterfaceNames, Map<String, String> hostProtocolInterfaceMap) {
        super(protocolInterfaceNames);
        this.mHostClassLoader = hostClassLoader;
        this.mHostProtocolInterfaceMap = hostProtocolInterfaceMap;
        this.mBlockProtocolInterfaceMap = swapKeyValue(mHostProtocolInterfaceMap);
    }

    private Map<String, String> swapKeyValue(Map<String, String> map) {
        if (map == null || map.isEmpty()) return map;
        HashMap<String, String> swapMap = new HashMap<>(map.size());
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            swapMap.put(entry.getValue(), entry.getKey());
        }
        return swapMap;
    }

    @Override
    public boolean isProtocolInterface(Class<?> interfaceClass) {
        String name = interfaceClass.getName();
        return super.isProtocolInterface(interfaceClass) || mHostProtocolInterfaceMap.containsValue(name);
    }

    @Override
    protected Class loadTargetProtocolClass(ClassLoader targetClassLoader, String protocolClassName) {
        Class cls;
        if (mHostClassLoader == targetClassLoader) {
            String name = protocolClassName;
            String hostProtocolInterfaceName = mHostProtocolInterfaceMap.get(name);
            if (!TextUtils.isEmpty(hostProtocolInterfaceName)) {
                name = hostProtocolInterfaceName;
            }
            cls = loadClass(targetClassLoader, name);
        } else {
            String name = protocolClassName;
            String blockProtocolInterfaceName = mBlockProtocolInterfaceMap.get(name);
            if (!TextUtils.isEmpty(blockProtocolInterfaceName)) {
                name = blockProtocolInterfaceName;
            }
            cls = loadClass(targetClassLoader, name);
        }
        return cls;
    }

    private Class loadClass(ClassLoader targetClassLoader, String name) {
        try {
            return targetClassLoader.loadClass(name);
        } catch (Exception e) {
            return null;
        }
    }
}