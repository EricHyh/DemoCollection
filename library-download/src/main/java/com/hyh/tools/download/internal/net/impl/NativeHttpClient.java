package com.hyh.tools.download.internal.net.impl;

import android.content.Context;

import com.hyh.tools.download.api.HttpCall;
import com.hyh.tools.download.api.HttpClient;
import com.hyh.tools.download.api.HttpResponse;

import java.io.IOException;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpClient implements HttpClient {

    private Context mContext;

    public NativeHttpClient(Context context) {
        mContext = context;
    }

    @Override
    public HttpCall newCall(String tag, String url, long oldSize) {
        return null;
    }

    @Override
    public HttpCall newCall(String tag, String url, long startPosition, long endPosition) {
        return null;
    }

    @Override
    public HttpResponse getHttpResponse(String url) throws IOException {
        return null;
    }
}
