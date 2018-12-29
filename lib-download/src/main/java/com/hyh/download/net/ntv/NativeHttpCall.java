package com.hyh.download.net.ntv;

import com.hyh.download.net.HttpCall;
import com.hyh.download.net.HttpCallback;
import com.hyh.download.net.HttpResponse;
import com.hyh.download.utils.L;
import com.hyh.download.utils.NetworkHelper;

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

    private static final int REQUEST_TIME_OUT = 15_000;

    private static final int READ_TIME_OUT = 15_000;

    private final Object mLock = new Object();

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

    HttpResponse execute() throws Exception {
        synchronized (mLock) {
            if (isExecuted) {
                throw new IllegalStateException("Already Executed");
            }
            isExecuted = true;

            if (isCanceled) {
                return null;
            }
            HttpURLConnection connection = getConnection(url, startPosition, endPosition);
            return new NativeHttpResponse(connection);
        }
    }

    @Override
    public void enqueue(final HttpCallback httpCallback) {
        synchronized (mLock) {
            if (isExecuted) {
                throw new IllegalStateException("Already Executed");
            }
            isExecuted = true;

            if (isCanceled) {
                httpCallback.onFailure(this, new IOException("Canceled"));
            } else {
                this.mHttpCallback = httpCallback;
                this.mRequestTask = new RequestTask();
                executor.execute(mRequestTask);
            }
        }
    }

    @Override
    public void cancel() {
        synchronized (mLock) {
            isCanceled = true;
            if (isExecuted) {
                if (mRequestTask.isRunning) {
                    mRequestTask.stop();
                } else {
                    executor.remove(mRequestTask);
                }
            }
        }
    }

    @Override
    public boolean isExecuted() {
        synchronized (mLock) {
            return isExecuted;
        }
    }

    @Override
    public boolean isCanceled() {
        synchronized (mLock) {
            return isCanceled;
        }
    }


    private HttpURLConnection getConnection(String url, long startPosition, long endPosition) throws IOException, IllegalAccessException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Encoding", "identity");
        connection.addRequestProperty("Connection", "keep-alive");
        connection.addRequestProperty("User-Agent", userAgent);
        connection.setConnectTimeout(REQUEST_TIME_OUT);
        connection.setReadTimeout(READ_TIME_OUT);
        if (startPosition >= 0) {
            if (endPosition < 0) {
                connection.setRequestProperty("Range", "bytes=" + startPosition + "-");
            } else {
                connection.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
            }
        }
        int code = connection.getResponseCode();
        String location = connection.getHeaderField("Location");
        if (NetworkHelper.isRedirect(code)) {
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

    private class RequestTask implements Runnable {

        private HttpURLConnection connection;
        private InputStream inputStream;
        private volatile boolean isRunning;

        @Override
        public void run() {
            isRunning = true;
            Exception exception = null;
            try {
                synchronized (this) {
                    connection = getConnection(url, startPosition, endPosition);
                    inputStream = connection.getInputStream();
                }
            } catch (Exception e) {
                exception = e;
            }
            if (exception == null) {
                HttpResponse httpResponse = new NativeHttpResponse(connection);
                try {
                    mHttpCallback.onResponse(NativeHttpCall.this, httpResponse);
                } catch (IOException e) {
                    L.d("onResponse failed", e);
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
