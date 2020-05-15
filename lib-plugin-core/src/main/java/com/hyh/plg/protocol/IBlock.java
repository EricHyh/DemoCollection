package com.hyh.plg.protocol;

import android.content.Context;

import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/3/16
 */

public interface IBlock {

    boolean onCreate(Context context, ClassLoader masterClassLoader, String blockPath, Map params, Object... masterClients);

    Object command(String command, Map params, Object... masterClients);

    Object getBlockProxy(String proxyKey, Map params, Object... masterClients);

    void setInnerProtocolInterfaceMap(Map<String, String> interfaceMap);

    Object getInnerBlockProxy(String proxyKey, Map params, Object... masterClients);

    void onDestroy();

}
