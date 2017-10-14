package com.hyh.tools.download.internal.net.impl;

import android.os.Build;

import com.hyh.tools.download.api.HttpCall;
import com.hyh.tools.download.api.HttpCallback;
import com.hyh.tools.download.api.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpCall implements HttpCall {


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

    private HttpURLConnection mConnection;
    private InputStream mInputStream;
    private boolean mIsExecuted;
    private boolean mIsCanceled;


    @Override
    public void enqueue(HttpCallback httpCallback) {
        mIsExecuted = true;
        try {
            mInputStream = mConnection.getInputStream();
            int responseCode = mConnection.getResponseCode();
            long contentLength;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentLength = mConnection.getContentLengthLong();
            } else {
                contentLength = mConnection.getContentLength();
            }
            HttpResponse httpResponse = new NativeHttpResponse(mInputStream, contentLength, responseCode);
            httpCallback.onResponse(this, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
            httpCallback.onFailure(this, e);
        }
    }

    @Override
    public void cancel() {
        mIsCanceled = true;
        try {
            if (mInputStream == null) {
                mInputStream = mConnection.getInputStream();
                mInputStream.close();
                mConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExecuted() {
        return mIsExecuted;
    }

    @Override
    public boolean isCanceled() {
        return mIsCanceled;
    }



    private static boolean isRedirect(int code) {
        return code == HttpURLConnection.HTTP_MOVED_PERM
                || code == HttpURLConnection.HTTP_MOVED_TEMP
                || code == HttpURLConnection.HTTP_SEE_OTHER
                || code == HttpURLConnection.HTTP_MULT_CHOICE
                || code == HTTP_TEMPORARY_REDIRECT
                || code == HTTP_PERMANENT_REDIRECT;
    }
}
