package com.hyh.plg.core;

import android.app.Application;

import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/3/16
 */

public interface IBlock {

    boolean prepare(Application application, ClassLoader hostClassLoader, Map<String, Object> params);

    boolean attachBaseContext(boolean isIndependentProcess);

    void onApplicationCreate();

    void setInnerProtocolInterfaceMap(Map<String, String> interfaceMap);

    Object command(String command, Map<String, Object> params, Object... masterClients);

    Object getInnerBlockProxy(String proxyKey, Map<String, Object> params, Object... masterClients);

    Object getBlockProxy(String proxyKey, Map<String, Object> params, Object... masterClients);

}