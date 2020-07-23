package com.hyh.plg.api;

import android.app.Application;

import com.hyh.plg.core.ParamWrapper;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2018/3/21
 */

public interface IBlockApplication {

    int getDefaultTheme();

    void afterAttach(Application hostApplication, Map<String, Object> params);

    List<String> getProtocolInterfaceNames();

    Object getBlockProxy(String proxyKey, Map<String, Object> params, ParamWrapper... paramWrappers);

}