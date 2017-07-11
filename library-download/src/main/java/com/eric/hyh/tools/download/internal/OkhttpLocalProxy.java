package com.eric.hyh.tools.download.internal;

import android.content.Context;

import com.eric.hyh.tools.download.api.HttpClient;


/**
 * @author Administrator
 * @description
 * @data 2017/5/16
 */

public class OkhttpLocalProxy extends LocalDownloadProxyImpl {

    public OkhttpLocalProxy(Context context, int maxSynchronousDownloadNum) {
        super(context, maxSynchronousDownloadNum);
    }

    @Override
    protected HttpClient getHttpClient() {
        return new HttpClient_Okhttp();
    }
}
