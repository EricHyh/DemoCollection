package com.hyh.download.net.ntv;

import android.content.Context;

import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpClient;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.NetworkHelper;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpClient implements HttpClient {

    private String mUserAgent;

    private ThreadPoolExecutor mExecutor;

    public NativeHttpClient(Context context) {
        this.mUserAgent = NetworkHelper.getUserAgent(context);
        this.mExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "NativeHttpClient");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @Override
    public HttpCall newCall(String tag, String url, long oldSize) {
        return new NativeHttpCall(url, mUserAgent, oldSize, -1, mExecutor);
    }

    @Override
    public HttpCall newCall(String tag, String url, long startPosition, long endPosition) {
        return new NativeHttpCall(url, mUserAgent, startPosition, endPosition, mExecutor);
    }

    @Override
    public HttpResponse getHttpResponse(String url) throws Exception {
        NativeHttpCall nativeHttpCall = new NativeHttpCall(url, mUserAgent, -1, -1, mExecutor);
        return nativeHttpCall.execute();
    }
}
