package com.hyh.plg.dispatch;

import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/3/21
 */

public interface BlockReceiver {

    List<String> getProtocolInterfaceNames();

    int getTheme();

    void onCreate(Context context, Map params, Object... masterClients);

    void command(String command, Map params, Object... masterProxies);

    Object getBlockProxy(String proxyKey, Map params, Object... masterProxies);

    void onDestroy();

}
