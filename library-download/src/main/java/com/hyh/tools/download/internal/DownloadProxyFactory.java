package com.hyh.tools.download.internal;

/**
 * @author Administrator
 * @description
 * @data 2017/7/10
 */

public interface DownloadProxyFactory {

    IDownloadProxy.ILocalDownloadProxy produce(boolean byService, int maxSynchronousDownloadNum);

}
