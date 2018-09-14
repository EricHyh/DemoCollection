package com.hyh.tools.download.net.nativeimpl;

import android.os.Build;
import android.text.TextUtils;

import com.hyh.tools.download.net.HttpCall;
import com.hyh.tools.download.net.HttpCallback;
import com.hyh.tools.download.net.HttpResponse;
import com.hyh.tools.download.utils.FD_LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 * @description
 * @data 2017/10/14
 */

public class NativeHttpCall implements HttpCall {


    private static final int MAX_REDIRECT_TIMES = 10;
    /**
     * The target resource resides temporarily under a different URI and the user agent MUST NOT
     * change the request method if it performs an automatic redirection to that URI.
     */
    private static final int HTTP_TEMPORARY_REDIRECT = 307;
    /**
     * The target resource has been assigned a new permanent URI and any future references to this
     * resource ought to use one of the enclosed URIs.
     */
    private static final int HTTP_PERMANENT_REDIRECT = 308;

    private static final int REQUEST_TIME_OUT = 15_000;

    private static final int READ_TIME_OUT = 15_000;


    private String url;
    private String userAgent;
    private long startPosition;
    private long endPosition;
    private ThreadPoolExecutor executor;
    private RequestTask mRequestTask;


    private HttpCallback mHttpCallback;
    private volatile boolean isExecuted;
    private volatile boolean isCanceled;
    private int redirectTimes;

    NativeHttpCall(String url, String userAgent, long startPosition, long endPosition, ThreadPoolExecutor executor) {
        this.url = url;
        this.userAgent = userAgent;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.executor = executor;
    }

    public HttpResponse execute() throws Exception {
        synchronized (this) {
            if (isExecuted) {
                throw new IllegalStateException("Already Executed");
            }
            isExecuted = true;
        }
        if (isCanceled) {
            return null;
        }
        HttpURLConnection connection = getConnection(url, startPosition, endPosition);
        return new NativeHttpResponse(connection.getInputStream(), getContentLength(connection),
                connection.getResponseCode());
    }

    @Override
    public void enqueue(final HttpCallback httpCallback) {
        synchronized (this) {
            if (isExecuted) {
                throw new IllegalStateException("Already Executed");
            }
            isExecuted = true;
        }
        if (isCanceled) {
            httpCallback.onFailure(this, new IOException("Canceled"));
        } else {
            this.mHttpCallback = httpCallback;
            this.mRequestTask = new RequestTask();
            executor.execute(mRequestTask);
        }
    }

    @Override
    public void cancel() {
        isCanceled = true;
        if (isExecuted) {
            if (mRequestTask.isRunning) {
                mRequestTask.stop();
            } else {
                executor.remove(mRequestTask);
            }
        }
    }

    @Override
    public synchronized boolean isExecuted() {
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

    private HttpURLConnection getConnection(String url, long startPosition, long endPosition) throws IOException, IllegalAccessException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Encoding", "identity");
        connection.addRequestProperty("Connection", "keep-alive");
        connection.addRequestProperty("User-Agent", userAgent);
        connection.setConnectTimeout(REQUEST_TIME_OUT);
        connection.setReadTimeout(READ_TIME_OUT);
        if (endPosition < 0) {
            connection.setRequestProperty("Range", "bytes=" + startPosition + "-");
        } else {
            connection.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
        }
        int code = connection.getResponseCode();
        String location = connection.getHeaderField("Location");
        if (isRedirect(code)) {
            if (++redirectTimes >= MAX_REDIRECT_TIMES) {
                throw new IllegalAccessException("redirect times reach max");
            }
            if (location == null) {
                String format = String.format(Locale.ENGLISH, "receive %d (redirect) but the location is null with " + "response [%s]",
                        code, connection.getHeaderFields());
                throw new IllegalAccessException(format);
            }
            return getConnection(location, startPosition, endPosition);
        }
        return connection;
    }

    private long getContentLength(HttpURLConnection connection) {
        long contentLength;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentLength = connection.getContentLengthLong();
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

    private class RequestTask implements Runnable {

        private HttpURLConnection connection;
        private InputStream inputStream;
        private volatile boolean isRunning;

        @Override
        public void run() {
            isRunning = true;
            Integer responseCode = null;
            Exception exception = null;
            try {
                synchronized (this) {
                    connection = getConnection(url, startPosition, endPosition);
                    inputStream = connection.getInputStream();
                }
                responseCode = connection.getResponseCode();
            } catch (Exception e) {
                exception = e;
            }
            if (exception == null) {
                HttpResponse httpResponse = new NativeHttpResponse(inputStream, getContentLength(connection), responseCode);
                try {
                    mHttpCallback.onResponse(NativeHttpCall.this, httpResponse);
                } catch (IOException e) {
                    FD_LogUtil.d("onResponse failed", e);
                }
            } else {
                mHttpCallback.onFailure(NativeHttpCall.this, exception);
            }
        }

        void stop() {
            synchronized (this) {
                try {
                    inputStream.close();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
