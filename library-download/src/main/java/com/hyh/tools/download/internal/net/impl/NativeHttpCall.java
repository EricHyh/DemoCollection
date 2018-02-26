package com.hyh.tools.download.internal.net.impl;

import android.os.Build;
import android.text.TextUtils;

import com.hyh.tools.download.api.HttpCall;
import com.hyh.tools.download.api.HttpCallback;
import com.hyh.tools.download.api.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpCall implements HttpCall, Runnable {


    private final static int MAX_REDIRECT_TIMES = 10;
    /**
     * The target resource resides temporarily under a different URI and the user agent MUST NOT
     * change the request method if it performs an automatic redirection to that URI.
     */
    private final static int HTTP_TEMPORARY_REDIRECT = 307;
    /**
     * The target resource has been assigned a new permanent URI and any future references to this
     * resource ought to use one of the enclosed URIs.
     */
    private final static int HTTP_PERMANENT_REDIRECT = 308;

    private ThreadPoolExecutor mExecutor;
    private HttpURLConnection mConnection;
    private InputStream mInputStream;
    private HttpCallback mHttpCallback;
    private volatile boolean isExecuted;
    private volatile boolean isCanceled;
    private volatile boolean isRunning;

    @Override
    public void enqueue(final HttpCallback httpCallback) {
        isExecuted = true;
        this.mHttpCallback = httpCallback;
        mExecutor.execute(this);
    }

    private long getContentLength(HttpURLConnection connection) {
        long contentLength;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentLength = mConnection.getContentLengthLong();
        } else {
            String contentLengthStr = connection.getHeaderField("content-length");
            if (!TextUtils.isEmpty(contentLengthStr) && TextUtils.isDigitsOnly(contentLengthStr)) {
                contentLength = Long.parseLong(contentLengthStr);
            } else {
                contentLength = connection.getContentLength();
            }
        }
        return contentLength;
    }

    @Override
    public void cancel() {
        isCanceled = true;
        if (isExecuted) {
            if (isRunning) {
                try {
                    mInputStream.close();
                    mConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mExecutor.remove(this);
            }
        }
    }

    @Override
    public boolean isExecuted() {
        return isExecuted;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }


    private static boolean isRedirect(int code) {
        return code == HttpURLConnection.HTTP_MOVED_PERM
                || code == HttpURLConnection.HTTP_MOVED_TEMP
                || code == HttpURLConnection.HTTP_SEE_OTHER
                || code == HttpURLConnection.HTTP_MULT_CHOICE
                || code == HTTP_TEMPORARY_REDIRECT
                || code == HTTP_PERMANENT_REDIRECT;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            mInputStream = mConnection.getInputStream();
            int responseCode = mConnection.getResponseCode();
            HttpResponse httpResponse = new NativeHttpResponse(mInputStream, getContentLength(mConnection), responseCode);
            mHttpCallback.onResponse(NativeHttpCall.this, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
            mHttpCallback.onFailure(NativeHttpCall.this, e);
        }
    }
}
